import requests
import threading
"""
	Esta classe recebe os dados provenientes das outras classes, realiza a conexão com o Spring, e os envia formatados em JSON.
	This class receives data from the other classes, establishes a connection with Spring, and sends the data formatted as JSON.
"""

class ApacheLogSender:

	"""
		Instancia com ip padrão localhost, porta padrão do Spring, path root e método http
		instance with the default localhost IP, default Spring port, the root path and http method
	"""
	def __init__(self, ip="127.0.0.1", port=8080, path="/", method="http"):

		self.port = port 
		self.ip = ip
		self.path = path
		self.method = method
		self.content = []

	#Define o array de JSONs ou um único JSON - Defines an JSON array or a unique JSON
	def setContent(self, content):
		
		self.content = content

	""" Retorna uma tupla, com um booleano e a resposta -- Returns a Tuple with a boolean and the response"""
	def send(self):

		if len(self.content) != 0:
			
			url = f"{self.method}://{self.ip}:{self.port}{self.path}"
			
			try:
				headers = {'Content-Type': 'application/json'}
				response = requests.post(url, json=self.content, headers=headers, timeout=5)
				response.raise_for_status()
				self.content.clear() #Clear to the next Send call -- Limpa para a próxima chamada
				return (True, response.json())

			#Erro de timeout de resposta -- Timeout response error
			except requests.exceptions.Timeout:
				return (False, "Timeout exceeded")

			#Erro interno do servidor -- Internal server error
			except requests.exceptions.HTTPError as error_http:
				return (False, "Server error: "+str(error_http))

			#Erro geral na conexão -- General conection error
			except requests.exceptions.RequestException as other_error:
				return (False, "General conection error: "+str(other_error))

		else:
			return (False, "None Content to Send")

	def send_async(self, callback_function):
		def wrapper():
			# Executa o send original e pega a tupla de retorno
			sucesso, info = self.send()
			# Entrega o retorno para a função de callback
			callback_function(sucesso, info)
		thread = threading.Thread(target=wrapper, daemon=True)
		thread.start()


def callback_api(sucesso, info):
    if sucesso:
        print(f"\n[Sucess]: {info}")
    else:
        print(f"\n[Error]: {info}")