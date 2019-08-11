import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import {Sensor} from "./sensor";
import {SensorUpdate} from "./sensorUpdate";

@Injectable({
  providedIn: 'root'
})
export class SensorService {

  constructor(private http: HttpClient) { }

  getSensors() {
    return this.http.get<Array<Sensor>>('/api/sensor');
  }

  getSensor(id: String) {
    return this.http.get<Sensor>('/api/sensor/' + id);
  }

  updateSensor(id: String, sensorUpdate: SensorUpdate) {
    console.info(id);
    return this.http.post('/api/sensor/' + id, sensorUpdate);
  }

}
