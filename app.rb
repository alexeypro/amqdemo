require "rubygems"
require "stomp"
require "rubygems"
require "sinatra"
require "memcache"

clientname = "client1234567"
queuereply = "/queue/" + clientname

get "/" do
  erb :index
end

get "/run" do  
  mc = MemCache.new('127.0.0.1:17898')
  client = Stomp::Client.open nil, nil, "localhost", 61613
  client.send("/queue/ScreenscrapBebo", "Hello", { "persistent" => true, "reply-to" => queuereply })
  
  if mc.get("stat" + clientname) == nil 
    mc.add("stat" + clientname, "0")
  else
    mc.replace("stat" + clientname, "0")
  end
  client.subscribe(queuereply, { "persistent" => true }) do |message|
    print "[" + clientname + "] Received reply: " + message.body + " \n"     
    mc.replace("stat" + clientname, "1")
    client.close      
  end
  redirect "/"
end

get "/status" do
  mc = MemCache.new('127.0.0.1:17898')
  stat = mc.get("stat" + clientname)
  if stat == nil
    return "We didn't even start!"
  else
    return "Done!" if stat.to_i == 1
    return "Still working..." if stat.to_i == 0
  end
end