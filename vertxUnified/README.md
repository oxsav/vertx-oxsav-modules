# Vert.x Unified Push Client

# Client (beta version)

This is a simple module that enables sending notifications over vert.x eventbus. 
## Name

The module name is `unifiedpushclient`.

## Configuration

The unifiedpushclient configuration is as follows:

    {
    	"address": <address>,
        "unifiedpushurl": <unifiedpushurl>,
        "unifiedport": <unifiedport>,
        "serverName": <serverName>,
        "proxy": <proxy>,
        "pushapplicationid": <pushapplicationid>,
        "mastersecret": <mastersecret>,
        "variants": <variants>
    }
    
* `address`. This is the address that you should send the messages. Default `vertx.unifiedpush`
* `unifiedpushurl`. This is the URL where the Unified Push Server is running. Default `localhost`
* `unifiedport`. This is the port number where the Unified Push Server is running. Default `8080`
* `serverName`. This is the server name that must be defined next to the unifiedpushurl and unifiedport. Default `unifiedpush-server-0.10.1` 
* `proxy`. The proxy is a boolean variable, define if the unified client must send notifications with proxy or without proxy. Default `false`
* `pushapplicationid`. Should contain the ID of the application where we want to send notifications. No Default 
* `mastersecret`. Should contain the mastersecret of the application where we want to send notifications. No Default 
* `variants`. Array of variants that belongs to the Application. No Default

# Message Fields

    {
        "action": "send",
        "alert": <message_to_send>,
        "variants": [array_of_variants],
        "aliases": [array_of_aliases]
    }

# JavaScript Example:
```javascript
	var message = {
		"action"    : "action",
		"alert"     : "message",
		"variants"  : ["variant1", "variant2", ..., "variantn"],
		"aliases"     : ["alias1", "alias2", ..., "aliasn"]
	};

	eb.send("vertx.unifiedpush", message, function(reply){ 
		console.log(reply);
	});
```
