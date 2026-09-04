import subprocess
import time
import sys
from pathlib import Path

sys.path.append(str(Path(__file__).resolve().parent.parent))

def iniciar_sistema():
    processos = []
    print("=" * 60)
    print(" INICIANDO ECOSSISTEMA DE LOGS ASSÍNCRONO ")
    print("=" * 60)

    try:
        # 1. Inicia o Servidor de Teste (Porta 8080)
        print("[SISTEMA] A iniciar o servidor_teste.py...")
        servidor = subprocess.Popen([sys.executable, "servidor_de_teste.py"])
        processos.append(servidor)
        time.sleep(1) # Aguarda o servidor subir

        # 2. Inicia o Gerador de Logs Falsos (Cria o access.log)
        print("[SISTEMA] A iniciar o gerador_logs.py...")
        gerador = subprocess.Popen([sys.executable, "gerador_de_log_falso.py"])
        processos.append(gerador)
        time.sleep(1) # Aguarda gerar a primeira linha

        # 3. Inicia o seu script principal (Monitorizador)
        print("[SISTEMA] A iniciar o main.py...")
        # Certifique-se de que o seu script principal se chama 'main.py'
        monitor = subprocess.Popen([sys.executable, "SentinelOps.py", "-lp", "exemplo.log"])
        processos.append(monitor)

        print("\n[SUCESSO] Todos os scripts estão a rodar em segundo plano!")
        print("Acesse no seu navegador: http://127.0.0")
        print("Pressione Ctrl+C para encerrar TODOS os scripts de uma vez.\n")

        # Mantém o script pai rodando enquanto os filhos estiverem ativos
        while True:
            time.sleep(1)

    except KeyboardInterrupt:
        print("\n" + "=" * 60)
        print("[SISTEMA] A encerrar todos os processos pendentes...")
        print("=" * 60)
        
        # Encerra cada um dos scripts filhos de forma segura
        for p in processos:
            try:
                p.terminate()
                p.wait(timeout=2)
            except Exception:
                p.kill() # Força a paragem se não fechar a bem
                
        print("[SISTEMA] Todos os processos foram finalizados com sucesso.")

if __name__ == "__main__":
    iniciar_sistema()
