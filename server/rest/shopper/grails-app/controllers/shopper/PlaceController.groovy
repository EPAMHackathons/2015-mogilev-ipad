package shopper

import grails.converters.JSON

class PlaceController {

    def loginService

    def index() {
        def store = Store.get(params.storeId as long)
        def places = Place.findAllByStore(store)
        render places as JSON
    }

    def create() {
        def json = request.JSON
        def user = User.get(loginService.findToken(json.token).userId)
        def place = new Place(longitude: json.longitude as double,
                              latitude: json.latitude as double,
                              user: user)
        def store = Store.get(json.storeId as long)
        store.addToPlaces(place)
        store.save(flush: true)

        JSON.use('deep')
        render place as JSON
    }
}
