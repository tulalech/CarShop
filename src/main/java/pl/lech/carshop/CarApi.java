package pl.lech.carshop;


import ch.qos.logback.classic.gaffer.PropertyUtil;
import ch.qos.logback.core.joran.util.beans.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.xml.ws.Response;
import java.lang.reflect.Field;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cars")
public class CarApi {

    private List<Car> carList;
    private Optional<Car> findCar;

    public CarApi(List<Car> carList) {
        this.carList = carList;
    }

    public CarApi() {
        this.carList = new ArrayList<Car>();
        carList.add(new Car(1, "Audi", "5", "red"));
        carList.add(new Car(2, "Fiat", "126", "blue"));
        carList.add(new Car(3, "Opel", "Mocca", "red"));
    }

    @GetMapping
    public ResponseEntity<List<Car>> getCars(@RequestParam(required = false, defaultValue = "") String color) {
        if (color.isEmpty()) {
            return new ResponseEntity<>(carList, HttpStatus.OK);
        } else {
            Predicate<Car> byColor = car -> car.getColor().equals(color);
            List<Car> cars = carList.stream().filter(byColor).collect(Collectors.toList());
            return new ResponseEntity<>(cars, HttpStatus.OK);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable int id) {
        Optional<Car> findCar = carList.stream().filter(car -> car.getId() == id).findFirst();
        if (findCar.isPresent()) {
            return new ResponseEntity<>(findCar.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity addCar(@RequestBody Car car) {
        boolean added = carList.add(car);
        if (added) {
            return new ResponseEntity(HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @PutMapping
    public ResponseEntity modifyCar(@RequestBody Car modifiedCar) {
        Optional<Car> findCar = carList.stream().filter(car -> car.getId() == modifiedCar.getId()).findFirst();
        if (findCar.isPresent()) {
            carList.remove(findCar);
            carList.add(modifiedCar);
            return new ResponseEntity<>(findCar.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @PatchMapping("/{id}")
    public ResponseEntity modifyCarField(@PathVariable int id,  @RequestBody Map<Object, Object> carFields) {
        Optional<Car> findCar = carList.stream().filter(car -> car.getId() == id).findFirst();
        if (findCar.isPresent()) {
            carFields.forEach((key, value) -> {
                Field carField = ReflectionUtils.findField(Car.class, (String) key);
                carField.setAccessible(true);
                ReflectionUtils.setField(carField, findCar.get(), value);
            });
            return new ResponseEntity<>(findCar.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteCar(@PathVariable int id) {
        Optional<Car> firstCar = carList.stream().filter(car -> car.getId() == id).findFirst();
        if (firstCar.isPresent()) {
            carList.remove(firstCar.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }
}
