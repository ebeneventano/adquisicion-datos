	package ar.com.untref.adquisicion.arduino.entidades;

public class Medicion {

	private Punto3D posicion;
	private Punto3D velocidad;
	private Punto3D aceleracion;

	public Medicion(Punto3D posicion, Punto3D velocidad, Punto3D aceleracion) {
		super();
		this.posicion = posicion;
		this.velocidad = velocidad;
		this.aceleracion = aceleracion;
	}

	public Punto3D getPosicion() {
		return posicion;
	}

	public Punto3D getVelocidad() {
		return velocidad;
	}

	public Punto3D getAceleracion() {
		return aceleracion;
	}
	
}
