import os
from collections import deque

"""
Esta classe recebe o caminho absoluto do log e o carrega

	- Para pegar no Linux, use dentro do diretório do arquivo: pwd
	- Para pegar no Windows, use dentro do diretório do arquivo: echo %cd%
	- Caminho padrão no Ubuntu: /var/log/apache2/access.log

self class receives the absolute path to the log and loads it

	- To get it on Linux, use the following command within the file's directory: pwd
	- To get it on Windows, use the following command within the file's directory: echo %cd%
	- Default path on Ubuntu: /var/log/apache2/access.log
"""

class ApacheLogLoader:

	def __init__(self, logPath):

		self.logPath = logPath
		self.logContent = list()
		self.lastLogLineLoaded = ""
		self.lastPosition = 0


	# Testa a existência do arquivo
	# Checks if the file exists
	def fileExists(self):

		return os.path.exists(self.logPath)


	# Testa se o arquivo é nulo/vazio
	# Checks if the file is empty
	def isEmpty(self):

		if os.path.getsize(self.logPath) > 0:
			return False
		else:
			return True


	# Carrega TODOS os novos logs desde a última leitura
	# Loads ALL new logs since the last read
	def loadLog(self):

		if self.fileExists() and not self.isEmpty():
			self.logContent.clear()
			
			with open(self.logPath, "r", encoding="utf-8") as log:
				# Vai para a última posição lida (na primeira execução, vai para o início 0)
				log.seek(self.lastPosition)
				
				# Lê apenas as linhas novas a partir dali
				for line in log:
					self.logContent.append(line)
				
				# Atualiza o ponteiro para o final atual do ficheiro
				self.lastPosition = log.tell()

				if self.logContent:
					self.lastLogLineLoaded = self.logContent[-1]
					return True

		return False


	#Verifica de forma eficiente se o tamanho do ficheiro aumentou.
	def hasChanged(self):
		
		if self.fileExists():
			current_size = os.path.getsize(self.logPath)
			# Se o tamanho atual for maior que o último ponteiro salvo, há novos logs
			return current_size > self.lastPosition
		return False