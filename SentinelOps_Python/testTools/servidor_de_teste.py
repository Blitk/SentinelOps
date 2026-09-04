from http.server import ThreadingHTTPServer, BaseHTTPRequestHandler 
import json
import os
from pathlib import Path

sys.path.append(str(Path(__file__).resolve().parent.parent))

HISTORICO_LOGS = []

class TestServerHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.end_headers()

        if not HISTORICO_LOGS:
            html_vazio = """<!DOCTYPE html>
            <html lang="pt-BR">
            <head><meta charset="UTF-8"><title>Dashboard SentinelOps</title>
            <style>body { font-family: Arial, sans-serif; background: #f4f4f9; text-align: center; padding: 50px; }</style></head>
            <body><h2>Aguardando conexão...</h2><p>Nenhum log foi enviado pelo script ainda.</p></body>
            </html>"""
            self.wfile.write(html_vazio.encode('utf-8'))
            return

        linhas_tabela = ""
        for idx, log in enumerate(reversed(list(HISTORICO_LOGS)), 1):
            linhas_tabela += f"""
            <tr>
                <td><strong>#{idx}</strong></td>
                <td><span class="badge-ip">{log.get('ip', '-')}</span></td>
                <td>{log.get('datetime', '-')}</td>
                <td><span class="badge-method">{log.get('method', '-')}</span></td>
                <td style="color: #555;">{log.get('url', '-')}</td>
                <td><strong style="color: {'#28a745' if int(log.get('status', 0)) < 400 else '#dc3545'}">{log.get('status', '-')}</strong></td>
                <td>{log.get('size', '-')} bytes</td>
            </tr>
            """

        html_completo = f"""<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <!-- NOVO: Atualiza a página automaticamente a cada 2 segundos -->
    <meta http-equiv="refresh" content="2">
    <title>Histórico Global de Logs - SentinelOps</title>
    <style>
        body {{ font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; margin: 30px; color: #333; }}
        .container {{ max-width: 1200px; margin: 0 auto; background: #fff; padding: 25px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); }}
        h1 {{ color: #0056b3; margin-top: 0; border-bottom: 2px solid #0056b3; padding-bottom: 10px; display: flex; justify-content: space-between; align-items: center; }}
        .counter {{ background: #0056b3; color: white; padding: 5px 15px; border-radius: 20px; font-size: 16px; }}
        table {{ width: 100%; border-collapse: collapse; margin-top: 20px; }}
        th, td {{ padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }}
        th {{ background-color: #f8f9fa; color: #555; font-weight: 600; }}
        tr:hover {{ background-color: #f1f3f5; }}
        .badge-ip {{ background: #e9ecef; padding: 4px 8px; border-radius: 4px; font-family: monospace; font-size: 13px; }}
        .badge-method {{ background: #007bff; color: white; padding: 2px 6px; border-radius: 3px; font-size: 11px; font-weight: bold; }}
    </style>
</head>
<body>
    <div class="container">
        <h1>Sistema de Monitorização Apache <span class="counter">Total: {len(HISTORICO_LOGS)} logs</span></h1>
        <p>Abaixo estão listadas todas as requisições capturadas em tempo real (Auto-refresh de 2s).</p>
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>IP do Cliente</th>
                    <th>Data/Hora</th>
                    <th>Método</th>
                    <th>Recurso Solicitado</th>
                    <th>Status</th>
                    <th>Tamanho</th>
                </tr>
            </thead>
            <tbody>
                {linhas_tabela}
            </tbody>
        </table>
    </div>
</body>
</html>
"""
        with open("index.html", "w", encoding="utf-8") as f:
            f.write(html_completo)

        self.wfile.write(html_completo.encode('utf-8'))

    def do_POST(self):
        try:
            content_length = int(self.headers['Content-Length'])
            post_data = self.rfile.read(content_length)
            
            try:
                dados_json = json.loads(post_data.decode('utf-8'))
                if isinstance(dados_json, list):
                    for item in dados_json:
                        HISTORICO_LOGS.append(item)
                else:
                    HISTORICO_LOGS.append(dados_json)
            except Exception as e:
                print(f"[SERVIDOR] -> Falha ao analisar JSON: {e}")

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            
            resposta_sucesso = json.dumps({"status": "sucesso", "total_acumulado": len(HISTORICO_LOGS)}).encode('utf-8')
            self.wfile.write(resposta_sucesso)

        except Exception as erro_geral:
            print(f"[SERVIDOR ERRO] -> {erro_geral}")
            self.send_response(500)
            self.end_headers()

def rodar_servidor():
    endereco = ('127.0.0.1', 8080)
    httpd = ThreadingHTTPServer(endereco, TestServerHandler)
    print("=" * 60)
    print("Servidor Multi-Thread Ativo em: http://127.0.0.1:8080/")
    print("=" * 60)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\nServidor finalizado.")

if __name__ == '__main__':
    rodar_servidor()
