package ar.com.untref.adquisicion.arduino.entidades;

public class Punto3D {

	private Double x = 0d;
	private Double y = 0d;
	private Double z = 0d;

	public Punto3D(){
	}
	
	public Punto3D(Double x, Double y, Double z) {
		super();
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Double getX() {
		return x;
	}

	public Double getY() {
		return y;
	}

	public Double getZ() {
		return z;
	}

}
