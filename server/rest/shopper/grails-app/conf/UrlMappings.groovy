class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }


        "404"(controller: 'error', action: 'error404')
        "405"(controller: 'error', action: 'error405')
        "500"(controller: 'error', action: 'error500')

        "/user"(controller: 'user', action: 'index', method: 'GET')
        "/user"(controller: 'user', action: 'login', method: 'POST')
        "/user/$id"(controller: 'user', action: 'show', method: 'GET')
        "/store"(controller: 'store', action: 'index', method: 'GET')
        "/store"(controller: 'store', action: 'create', method: 'POST')
	}
}
