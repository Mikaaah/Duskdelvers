#!/usr/bin/env python3
"""Start a real headless NeoForge dev server, then address the Chaos Caverns via RCON."""
import os, pathlib, re, signal, socket, struct, subprocess, sys, time

root = pathlib.Path.cwd()
run = root / "run"
run.mkdir(exist_ok=True)
(run / "eula.txt").write_text("eula=true\n")
(run / "server.properties").write_text(
    "enable-rcon=true\nrcon.password=duskdelvers-ci-test-only\nrcon.port=25575\n"
    "online-mode=false\nmax-tick-time=-1\nlevel-name=world-smoke\n"
    "spawn-protection=0\nmax-players=1\n")
log = root / "server-smoke.log"
proc = None
success = False

def recv_packet(sock):
    size_bytes = sock.recv(4)
    if len(size_bytes) != 4:
        raise RuntimeError("RCON connection closed")
    size = struct.unpack("<i", size_bytes)[0]
    body = b""
    while len(body) < size:
        chunk = sock.recv(size - len(body))
        if not chunk: raise RuntimeError("RCON truncated response")
        body += chunk
    ident, typ = struct.unpack("<ii", body[:8])
    return ident, typ, body[8:-2].decode("utf-8", "replace")

def send_packet(sock, ident, typ, body):
    payload = struct.pack("<ii", ident, typ) + body.encode() + b"\x00\x00"
    sock.sendall(struct.pack("<i", len(payload)) + payload)

try:
    with log.open("w") as fh:
        proc = subprocess.Popen(
            ["gradle", "runServer", "--no-daemon"],
            stdout=fh, stderr=subprocess.STDOUT, start_new_session=True)
        for second in range(180):
            if proc.poll() is not None:
                print("Server process exited early with", proc.returncode)
                break
            contents = log.read_text(errors="replace")
            if "Done (" in contents or re.search(r"Done \(.*\)! For help", contents):
                print("Server startup completed after", second, "seconds")
                break
            time.sleep(1)
        else:
            raise RuntimeError("Server startup exceeded 180 seconds")

    if "Done (" not in log.read_text(errors="replace"):
        raise RuntimeError("NeoForge server never reached ready state")
    if "Parsing error loading recipe duskdelvers:delvers_compass" in log.read_text(errors="replace"):
        raise RuntimeError("Delver's Compass survival crafting recipe failed to load")

    with socket.create_connection(("127.0.0.1", 25575), timeout=15) as sock:
        sock.settimeout(15)
        send_packet(sock, 7, 3, "duskdelvers-ci-test-only")
        auth = recv_packet(sock)
        if auth[0] == -1:
            raise RuntimeError("RCON auth failed")
        send_packet(sock, 8, 2, "execute in duskdelvers:chaos_caverns run summon minecraft:zombie 0 75 0")
        response = recv_packet(sock)
        print("Dimension command response:", response)
        if response[0] == -1 or "Unknown dimension" in response[2] or "Unknown" in response[2]:
            raise RuntimeError("Chaos Caverns failed command dimension validation")

        # Verify all five player-built portals and both horizontal orientations.
        frames = {
            1: "mossy_stone_bricks",
            2: "prismarine_bricks",
            3: "amethyst_block",
            4: "polished_blackstone_bricks",
            5: "crying_obsidian",
        }
        command_id = 100

        def rcon_command(command):
            nonlocal_command = command  # Keep the command visible in failure diagnostics.
            global command_id
            command_id += 1
            send_packet(sock, command_id, 2, nonlocal_command)
            ident, typ, answer = recv_packet(sock)
            if ident != command_id:
                raise RuntimeError(f"RCON unexpected response id for {command}: {ident}")
            print(f"RCON: {command} => {answer.strip()}")
            return answer

        # Command blocks cannot be placed in unloaded chunks. Explicitly load the
        # compact test fixture region instead of relying on randomized spawn chunks.
        result = rcon_command("execute in minecraft:overworld run forceload add 0 0 64 32")
        print("Forceload result:", result)

        for tier, frame in frames.items():
            horizontal_x = tier % 2 == 1
            bx, by, bz = 6 + tier * 9, 85, 24
            coords = lambda u, v: (bx + u, by + v, bz) if horizontal_x else (bx, by + v, bz + u)
            def at(u, v):
                return " ".join(map(str, coords(u, v)))
            def place(u, v, block):
                return f"execute in minecraft:overworld run setblock {at(u,v)} minecraft:{block}"
            for u in range(4):
                for v in range(5):
                    block = frame if (u in (0,3) or v in (0,4)) else "air"
                    rcon_command(place(u,v,block))
            result = rcon_command(
                f"execute in minecraft:overworld run duskdelvers portal activate {at(0,0)}")
            if "Expedition portal activated." not in result:
                raise RuntimeError(f"Tier {tier} portal failed activation: {result}")
            portal = f"duskdelvers:tier_{tier}_portal"
            rcon_command(place(5,1,"air"))
            result = rcon_command(
                f"execute in minecraft:overworld if block {at(1,1)} {portal} run setblock {at(5,1)} minecraft:emerald_block")
            if "Changed the block" not in result:
                raise RuntimeError(f"Tier {tier} portal interior wasn't created: {result}")

            # Breaking a frame side must invalidate all linked interior blocks.
            rcon_command(place(0,2,"air"))
            rcon_command(place(5,2,"air"))
            result = rcon_command(
                f"execute in minecraft:overworld if block {at(1,2)} minecraft:air run setblock {at(5,2)} minecraft:diamond_block")
            if "Changed the block" not in result:
                raise RuntimeError(f"Tier {tier} portal didn't extinguish on frame break: {result}")

        print("All five portals passed activation, orientation and frame-break tests.")

        limits = rcon_command("duskdelvers limits")
        for expected in ("outpost=30m", "Tier 1=20m", "Tier 2=25m",
                         "Tier 3=30m", "Tier 4=35m", "Tier 5=40m",
                         "extraction grace=3m"):
            if expected not in limits:
                raise RuntimeError(f"Missing or incorrect configured time limit: {expected}. Got {limits!r}")
        print("All six dimension/expedition time limit defaults passed server validation.")
        daily = rcon_command("duskdelvers daily")
        assert "Maximum team size: 4" in daily, daily
        assert "16 blocks" in daily, daily
        compatibility = rcon_command("duskdelvers compat")
        assert "Detected optional mods:" in compatibility, compatibility
        print("Daily quota and optional-mod diagnostics passed.")
        # New cave-only generation: registry lookup does not depend on any
        # Twilight Forest or Aether installation.
        for biome in ("mosslit_grotto", "twilight_thicket", "aether_glades",
                      "golden_sanctuary", "chaos_rift"):
            response = rcon_command("execute in duskdelvers:chaos_caverns run locate biome duskdelvers:" + biome)
            if "Could not find" in response or "Unknown" in response or "invalid" in response.lower():
                raise RuntimeError("Missing or unreachable custom cave biome " + biome + ": " + response)
        print("Registered cave biome search passed for Tiers I-V.")

        # Ensure all five natural cache tables actually load and roll.
        for tier in range(1, 6):
            response = rcon_command(
                f"execute in duskdelvers:chaos_caverns run loot spawn 0 80 0 loot duskdelvers:chests/cavern_cache_tier_{tier}")
            if "Unknown" in response or "invalid" in response.lower():
                raise RuntimeError("Natural cave loot table missing for Tier " + str(tier) + ": " + response)
        print("All five biome-scaled natural cache loot tables resolved.")



    success = True
finally:
    if proc is not None and proc.poll() is None:
        os.killpg(proc.pid, signal.SIGTERM)
        try: proc.wait(timeout=20)
        except subprocess.TimeoutExpired:
            os.killpg(proc.pid, signal.SIGKILL)
    if not success:
        print("--- server log tail ---")
        print(log.read_text(errors="replace")[-8000:] if log.exists() else "<missing log>")
    sys.exit(0 if success else 1)
