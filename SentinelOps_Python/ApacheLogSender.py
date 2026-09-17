import requests
import threading

"""
Esta classe recebe um evento, realiza a conexão com o Spring
e envia o evento formatado em JSON.
"""


class ApacheLogSender:

    def __init__(
        self,
        ip="127.0.0.1",
        port=8080,
        path="/api/v1/events",
        method="http"
    ):

        self.port = port
        self.ip = ip
        self.path = path
        self.method = method
        self.content = None

    # Define UM evento JSON
    def setContent(self, content):

        self.content = content

    # Envia UM evento
    def send(self):

        if not self.content:
            return (False, "None Content to Send")

        url = f"{self.method}://{self.ip}:{self.port}{self.path}"

        headers = {
            "Content-Type": "application/json"
        }

        try:

            response = requests.post(
                url,
                json=self.content,
                headers=headers,
                timeout=5
            )

            response.raise_for_status()

            try:
                result = response.json()
            except ValueError:
                result = response.text

            self.content = None

            return (True, result)

        except requests.exceptions.Timeout:

            return (False, "Timeout exceeded")

        except requests.exceptions.HTTPError as error_http:

            return (
                False,
                "Server error: " + str(error_http)
            )

        except requests.exceptions.RequestException as other_error:

            return (
                False,
                "General connection error: " + str(other_error)
            )

    def send_async(self, callback_function):

        def wrapper():

            sucesso, info = self.send()

            callback_function(sucesso, info)

        thread = threading.Thread(
            target=wrapper,
            daemon=True
        )

        thread.start()


def callback_api(sucesso, info):

    if sucesso:
        print(f"\n[Success]: {info}")
    else:
        print(f"\n[Error]: {info}")
