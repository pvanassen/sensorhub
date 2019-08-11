export class SensorUpdate {
  name: String
  domoticsId: number


  constructor(name: String, domoticsId: number) {
    this.name = name;
    this.domoticsId = domoticsId;
  }
}
