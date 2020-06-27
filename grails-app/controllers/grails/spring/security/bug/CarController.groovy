package grails.spring.security.bug

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.*

class CarController {

    static scaffold = Car
    
    def update(Car car) {
    	println "Saving"
        request.now = new Date()        
        if (car == null) {
            notFound()
            return
        }

        try {
            car.save()
        } catch (ValidationException e) {
            respond car.errors, view:'edit'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'car.label', default: 'Car'), car.id])
                redirect car
            }
            '*'{ respond car, [status: OK] }
        }
    }
}
