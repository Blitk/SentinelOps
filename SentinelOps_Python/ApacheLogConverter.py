import json
import re

"""

Converte um arquivo ou uma linha de um output Apache acess.log em JSON
Converts a file or an output line from an Apache `access.log` to JSON.

"""
class ApacheLogConverter:

	# Define o padrão Regex para capturar os campos
	# Defines the Regex pattern to capture the fields
	def __init__(self):

		self.pattern = re.compile(
		    r'(?P<ip>\S+)\s+\S+\s+\S+\s+\['
		    r'(?P<datetime>[^\]]+)\]\s+"'
		    r'(?P<method>\S+)\s+(?P<url>\S+)\s+(?P<protocol>[^"]+)"\s+'
		    r'(?P<status>\d+)\s+'
		    r'(?P<size>\d+)\s+"'
		    r'(?P<referrer>[^"]*)"\s+"'
		    r'(?P<user_agent>[^"]*)"'
		)


	# Converte uma linha do Log em JSON
	# Convert one line of the log into JSON
	def convertLine(self, content):

		match = self.pattern.match(content)

		if match:

		    log_dict = match.groupdict()
		    
		    log_dict["status"] = int(log_dict["status"])
		    log_dict["size"] = int(log_dict["size"])
		    
		    return json.dumps(log_dict, indent=4, ensure_ascii=False)
		    
		else:
		    return False


	# Converte todo o conteúdo em JSON
	# Converts all the content into JSON
	def convertAll(self, content):

		data = list()

		for line in content:

			js = self.convertLine(line)
			if js == False:
				pass

			else:
				data.append(js)

		if len(data) == 0:
			return False
		else:
			return data

