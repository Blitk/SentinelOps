import random
import time
from datetime import datetime
from pathlib import Path

sys.path.append(str(Path(__file__).resolve().parent.parent))

# Configuração do ficheiro de destino (Altere para o mesmo caminho que vai passar no -lp)
NOME_FICHEIRO_LOG = "exemplo.log"

# Dados falsos para sorteio
IP_LIST = ["192.168.1.50", "10.0.0.12", "172.16.254.1", "200.150.45.22", "8.8.8.8"]
METHODS = ["GET", "POST", "PUT", "DELETE"]
RESOURCES = [
    "/index.html", 
    "/api/v1/users", 
    "/login", 
    "/assets/style.css", 
    "/images/logo.png", 
    "/favicon.ico"
]
STATUS_CODES = [200, 201, 304, 404, 500, 401]
USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15",
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36"
]

def gerar_linha_log():
    """Gera uma linha no formato padrão (Combined Log Format) do Apache."""
    ip = random.choice(IP_LIST)
    timestamp = datetime.now().strftime("%d/%b/%Y:%H:%M:%S %z")
    # Formato de fuso horário padrão do Apache se vazio (+0000)
    if not timestamp.endswith(" "):
        timestamp += " -0300" 
        
    method = random.choice(METHODS)
    resource = random.choice(RESOURCES)
    status = random.choice(STATUS_CODES)
    size = random.randint(100, 5000) if status == 200 else 0
    ua = random.choice(USER_AGENTS)
    
    return f'{ip} - - [{timestamp}] "{method} {resource} HTTP/1.1" {status} {size} "-" "{ua}"\n'

def rodar_gerador():

    
    try:
        while True:
            linha = gerar_linha_log()
            
            # Abre em modo "a" (append) para adicionar no fim do ficheiro
            with open(NOME_FICHEIRO_LOG, "a", encoding="utf-8") as f:
                f.write(linha)
                
            # Exibe no terminal do gerador para sabermos que está a funcionar
            print(f"[NOVO LOG FLUSHED]: {linha.strip()}")
            
            # Aguarda 2 segundos antes de gerar a próxima linha
            time.sleep(2)
            
    except KeyboardInterrupt:
        print("\nGerador finalizado.")

if __name__ == "__main__":
    rodar_gerador()
