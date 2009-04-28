Simple demo of Java on the backend, Ruby w/ Sinatra framework on the frontend, communicating asynchronously through ActiveMQ, and a little help from memcached.

Assuming you have ActiveMQ download and installed in /usr/local/activemq:

# sudo /usr/local/activemq/bin/activemq &
# sudo port install memcached libmemcached
# memcached -d -l 127.0.0.1 -p 17898 -m 256 -P /tmp/memcached.pid

# sudo gem install sinatra
# sudo gem install memcache-client

# ./c.sh
# ./r.sh
....

# ruby app.rb
...