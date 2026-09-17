import argparse
import sys
import time

from ApacheLogLoader import ApacheLogLoader
from ApacheLogSender import ApacheLogSender, callback_api
from ApacheLogConverter import ApacheLogConverter
import miscelanious


parser = argparse.ArgumentParser(description="SentinelOps")

parser.add_argument("-lp", required=True, help="Log path")
parser.add_argument("-ip", default="127.0.0.1", help="API's IP")
parser.add_argument("-pt", default="8080", help="API's Port")
parser.add_argument("-ph", default="/api/v1/events", help="API's path")
parser.add_argument("-mt", default="http", help="API's method")

args = parser.parse_args()


miscelanious.show(
    miscelanious.LOGO,
    args.lp,
    f"{args.mt}://{args.ip}:{args.pt}{args.ph}"
)


loader = ApacheLogLoader(args.lp)
converter = ApacheLogConverter()

if not loader.fileExists():
    print("Log file does not exist")
    sys.exit(1)


print("[MONITOR] Aguardando novos logs...")


# ---------------------------------------------------------
# Ignora os logs existentes na primeira execução.
# O monitor começará a partir dos novos logs.
# ---------------------------------------------------------

loader.loadLog()
loader.logContent.clear()


# ---------------------------------------------------------
# Monitoramento contínuo
# ---------------------------------------------------------

while True:

    # Verifica se existem novos logs
    if loader.hasChanged():

        if loader.loadLog():

            # Processa cada linha individualmente
            for line in loader.logContent:

                # Converte UMA linha em UM JSON
                event = converter.convertLine(line)

                if not event:
                    print("[WARNING] Linha ignorada:")
                    print(line)
                    continue

                # Cria um sender para este evento
                sender = ApacheLogSender(
                    args.ip,
                    int(args.pt),
                    args.ph,
                    args.mt
                )

                # Envia apenas UM evento
                sender.setContent(event)

                # Envio assíncrono
                sender.send_async(callback_api)

                print(
                    f"[MONITOR] Evento enviado: "
                    f"{event.get('sourceip')} "
                    f"{event.get('statuscode')}"
                )

            # Limpa depois de processar todas as linhas
            loader.logContent.clear()

    # Aguarda antes da próxima verificação
    time.sleep(2)
