require "rubygems"
require "stomp"
require "rubygems"
require "sinatra"
require "memcache"

# just define it on the top - clients name and queue where we will listen for replies
clientname = "client1234567"
queuereply = "/queue/" + clientname

get "/" do
  erb :index
end

get "/run" do  
  mc = MemCache.new('127.0.0.1:17898')
  client = Stomp::Client.open nil, nil, "localhost", 61613
  
  # send "Hello" to server listening on queue right away
  client.send("/queue/ScreenscrapBebo", "Hello", { "persistent" => true, "reply-to" => queuereply })
  
  if mc.get("stat" + clientname) == nil 
    mc.add("stat" + clientname, "0")
  else
    mc.replace("stat" + clientname, "0")
  end
  
  # here we prepare ourselves to listen what server replies us
  client.subscribe(queuereply, { "persistent" => true }) do |message|
    print "[" + clientname + "] Received reply: " + message.body + " \n"     
    mc.replace("stat" + clientname, "1")
    client.close      
  end
  
  # back to index, giving you opportunity to monitor status
  redirect "/"
end

get "/status" do
  mc = MemCache.new('127.0.0.1:17898')
  stat = mc.get("stat" + clientname)
  if stat == nil
    # you never ran /run, man
    return "We didn't even start!"
  else
    # something happened, and we may got a response already
    return "Done!" if stat.to_i == 1
    return "Still working..." if stat.to_i == 0
  end
end