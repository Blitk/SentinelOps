import argparse
import sys
import time
from ApacheLogLoader import ApacheLogLoader
from ApacheLogSender import ApacheLogSender, callback_api
from ApacheLogConverter import ApacheLogConverter

parser = argparse.ArgumentParser(description="SentinelOps")

parser.add_argument("-lp", required=True, help="Log path")
parser.add_argument("-ip", default="127.0.0.1", help="API's IP")
parser.add_argument("-pt", default="8080", help="API's Port")
parser.add_argument("-ph", default="/", help="API's path")
parser.add_argument("-mt", default="http", help="API's method")

args = parser.parse_args()

print(f"Monitorizando o ficheiro: {args.lp}")
print(f"Destino da API: {args.mt}://{args.ip}:{args.pt}{args.ph}")

loader = ApacheLogLoader(args.lp)

if not loader.fileExists():
    print("Log file does not exist")
    sys.exit(1)

converter = ApacheLogConverter()
sender = ApacheLogSender(args.ip, int(args.pt), args.ph, args.mt)

loader.loadLog()
loader.logContent.clear() 

print("[MONITOR] Aguardando novos logs...")

while True:
    # 1. Se houver novos logs capturados na memória
    if loader.logContent:
        # Converte as linhas atuais
        c = converter.convertAll(loader.logContent)

        sender_thread = ApacheLogSender(args.ip, int(args.pt), args.ph, args.mt)
        sender_thread.setContent(c)
        
        # Dispara o envio assíncrono com segurança
        sender_thread.send_async(callback_api)
        print(f"[MONITOR] Envio assíncrono disparado para {len(c)} linhas.")
        
        # Limpa imediatamente para não reprocessar as mesmas linhas no próximo ciclo
        loader.logContent.clear()

    # 2. Aguarda o intervalo de checagem
    time.sleep(2)

    # 3. Verifica se o ficheiro cresceu no disco
    if loader.hasChanged():
        loader.loadLog()
