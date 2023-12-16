local template = require "resty.template"

 content={ 
	message = "Hello, World!",
	names = {"aaa","bbb","ccc"}
}
-- openresty/nginx/html/templates/demo.html
template.render("demo.html", content)  