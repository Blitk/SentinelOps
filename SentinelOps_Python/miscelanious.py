import os, shutil, time

LOGO = r"""
███████╗███████╗███╗   ██╗████████╗██╗███╗   ██╗███████╗██╗      ██████╗ ██████╗ ███████╗
██╔════╝██╔════╝████╗  ██║╚══██╔══╝██║████╗  ██║██╔════╝██║     ██╔═══██╗██╔══██╗██╔════╝
███████╗█████╗  ██╔██╗ ██║   ██║   ██║██╔██╗ ██║█████╗  ██║     ██║   ██║██████╔╝███████╗
╚════██║██╔══╝  ██║╚██╗██║   ██║   ██║██║╚██╗██║██╔══╝  ██║     ██║   ██║██╔═══╝ ╚════██║
███████║███████╗██║ ╚████║   ██║   ██║██║ ╚████║███████╗███████╗╚██████╔╝██║     ███████║
╚══════╝╚══════╝╚═╝  ╚═══╝   ╚═╝   ╚═╝╚═╝  ╚═══╝╚══════╝╚══════╝╚═════╝ ╚═╝     ╚══════╝
"""

def show(text, apache_path, spring_path):
    os.system("cls" if os.name == "nt" else "clear")
    w = shutil.get_terminal_size((80, 20)).columns
    print("\n\n")
    for line in text.strip("\n").split("\n"):
        print(line.center(w))
        time.sleep(0.2)
    subtitle = f""" [ PROJECT ] SentinelOps - Mini SIEM v1.0.0
 [ OBJECT  ] Infrastructure Log Collector & Event Detection
 [ STUDY   ] Backend, Cybersecurity & Security Engineering
 ─────────────────────────────────────────────────────────────
 [ STATUS ] INITIALIZING PYTHON AGENT...
 [ STEP 1 ] Binding to Apache log files ({apache_path})
 [ STEP 2 ] Instantiating JSON data transformation engine
 [ STEP 3 ] Establishing handshake with Spring Boot API ({spring_path})...
 ─────────────────────────────────────────────────────────────
 [>] SYSTEM RUNNING. MONITORING ACTIVE TRAFFIC...
 (Press CTRL+Z to stop )
 """
    print("\n \n")
    for c in subtitle.strip("\n").split("\n"):
        print(c)
        time.sleep(0.2)
