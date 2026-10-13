export interface PilotBoats {
  id?: number;
  vesselCallSign?: string;
  boatName?: string;
  imoNumber?: string;
  operationRange?: string;
  engineModel?: string;
  enginePower?: string;
  rpmThrustForward?: string;
  rpmThrustReverse?: string;
  towingForce?: string;
  navigationEquipment?: string;
  buildDate?: string;
  shipyard?: string;
  maintenanceRecord?: string;
  currentStatus?: any;
  currentLocation?: string;
  maxSpeed?: number;
  passengerCapacity?: number;
  lastMaintenanceDate?: string;
  createdAt?: string;
  updatedAt?: string;
}