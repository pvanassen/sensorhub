import { Component, OnInit } from '@angular/core';
import {Sensor} from "../sensor";
import {SensorService} from "../sensor.service";
import {SensorUpdate} from "../sensorUpdate";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {map, switchMap} from "rxjs/operators";
import {Observable} from "rxjs";
import {FormBuilder} from "@angular/forms";

@Component({
  selector: 'app-update',
  templateUrl: './update.component.html',
  styleUrls: ['./update.component.scss']
})
export class UpdateComponent implements OnInit {
  sensorForm;
  sensor$: Observable<Sensor>;
  id: String;

  constructor(private route: ActivatedRoute,
              private router: Router,
              private sensorService: SensorService,
              private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.sensor$ = this.route.paramMap.pipe(
      switchMap((params: ParamMap) => this.sensorService.getSensor(params.get('id'))))
      .pipe(
        map(sensor => {
          this.id = sensor.id;
          this.sensorForm.name = sensor.name;
          this.sensorForm.domoticsId = sensor.domoticsId;
          return sensor;
        })
    );
    this.sensorForm = this.formBuilder.group({
      id: '',
      name: '',
      domoticsId: ''
    });
  }

  onSubmit(formData) {
    console.warn('Your order has been submitted', formData);

    this.update(this.id, formData.name, formData.domoticsId);
  }


  update(id:String, name: String, domoticsId: number) {
    this.sensorService.updateSensor(id, new SensorUpdate(name, domoticsId))
      .subscribe(_ => this.router.navigate(['/']))
  }


}
