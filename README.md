Environment Tested - Grails 4.1.0.M1 / Spring Security Core Plugin 4.0.0

GrailsHttpPutFormContentFilter.HttpPutFormContentRequestWrapper should allow setting request attributes via Groovy.
e.g. request.myattribute = 1234

```
% grails create-app grails-spring-security-bug
% cd grails-spring-security-bug
% grails create-domain-class Car
% grails create-scaffold-controller Car
```

Add method to CarController.groovy
```
    def update(Car car) {
        println "Saving"
        request.now = new Date()        
        if (car == null) {
            notFound()
            return
        }

        try {
            carService.save(car)
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

```

Add spring-security-core to build.gradle
`
compile "org.grails.plugins:spring-security-core:4.0.0" 
`
```
% grails s2-quickstart com.yourapp User Role
```
make example app less secure by modifying application.groovy:
```
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/**',              access: ['permitAll']]
]
```

REST works but
```
% grails run-app
% curl -i -X POST -H "Content-Type: application/json" -d '{}' localhost:8080/car/save.json 
% curl -i -X PUT -H "Content-Type: application/json" -d '{}' localhost:8080/car/update/1.json 
```

BUT, doing some jquery like this:
```
$.ajax({type: 'PUT', url: 'localhost:8080/car/update/1.json', data: {name:'Jeep'}})
```

which can be replicated by this:
```
curl -X PUT -H "application/x-www-form-urlencoded" -d '{}' localhost:8080/car/update/1.json 
```

causes this:
```
Caused by: groovy.lang.MissingPropertyException: No such property: now for class: grails.plugin.springsecurity.web.filter.GrailsHttpPutFormContentFilter
        at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at grails.spring.security.bug.CarController.update(CarController.groovy:12)
        ... 40 common frames omitted
```


Workaround:
```
    def update(Car car) {
        request.setAttribute('now', new Date()) // insert 1 line
```

The workaround is annoying because the request.xxx syntax works everywhere else except PUT requests due to them being wrapped with this incomplete wrapper.

Permanent fix, implement a groovy setProperty method for GrailsHttpPutFormContentFilter.HttpPutFormContentRequestWrapper
