	package ar.com.untref.adquisicion.arduino.entidades;

public class Medicion {

	private Punto3D posicion;
	private Punto3D velocidad;

	public Medicion(Punto3D posicion, Punto3D velocidad) {
		super();
		this.posicion = posicion;
		this.velocidad = velocidad;
	}

	public Punto3D getPosicion() {
		return posicion;
	}

	public Punto3D getVelocidad() {
		return velocidad;
	}
}
