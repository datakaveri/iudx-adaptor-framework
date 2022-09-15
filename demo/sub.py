import pika, sys, os

def main():
    connection = pika.BlockingConnection(pika.URLParameters(f'amqps://bcd3ccae-0fbb-46e0-9dea-dbadf0ecfb9f:2Xq2rLC9Qg2_RJXDlKV08A@databroker.iudx.org.in/IUDX'))
    channel = connection.channel()


    def callback(ch, method, properties, body):
        print(" [x] Received %r" % body)

    channel.basic_consume(queue='bcd3ccae-0fbb-46e0-9dea-dbadf0ecfb9f/titan-itms', on_message_callback=callback, auto_ack=True)

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
