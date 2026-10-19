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
