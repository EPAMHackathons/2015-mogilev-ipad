package shopper

import grails.converters.JSON

class UserController {

    def loginService

    def index() {
        JSON.use('deep')
        render User.list() as JSON
    }

    def show() {
        JSON.use('deep')
        render User.get(params.id) as JSON
    }

    def login() {
        def json = request.JSON
        def googleId = json.id
        def user = User.findByGoogleId(googleId)
        if(!user) {
            user = new User(googleId: googleId,
                            name: json.name,
                            firstName: json.given_name,
                            lastName: json.family_name,
                            avatarLink: json.picture,
                            gplusAccountLink: json.link,
                            locale: json.locale,
                            gender: json.gender).save(flush: true)
        }

        def token = loginService.getToken(user)
        render token as JSON
    }
}
