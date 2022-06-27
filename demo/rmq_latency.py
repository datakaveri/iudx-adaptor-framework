import pika, sys, os, json
from dateutil import tz
import time
from datetime import datetime

routing_key = sys.argv[1]

def main():
    connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
    channel = connection.channel()


    def callback(ch, method, properties, body):
        if routing_key == method.routing_key:
            data = json.loads(body.decode())
            current_time = time.time()
            obsereved_time = datetime.strptime(data['observationDateTime'], "%Y-%m-%dT%H:%M:%S.%fZ")
            from_zone = tz.tzutc()
            to_zone = tz.tzlocal()
            utc_time = obsereved_time.replace(tzinfo=from_zone)
            obsereved_time = utc_time.astimezone(to_zone).timestamp()
            print(current_time - obsereved_time)
            

    channel.basic_consume(queue='adaptor-test', on_message_callback=callback, auto_ack=True)

    print(' [*] Waiting for messages. To exit press CTRL+C')
    channel.start_consuming()

if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        print('Interrupted')
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
