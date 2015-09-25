package ar.com.untref.adquisicion.arduino.entidades;

public class Lectura {

	// 257.36|2|0|2292|8694|95.332|1|2|-5888

	private Double inclinacion = 0.0;
	private Integer error = 0;
	private Double aceleracionX = 0.0;
	private Double aceleracionY = 0.0;
	private Double aceleracionZ = 0.0;
	private Double temperatura = 0.0;
	private Double giroX = 0.0;
	private Double giroY = 0.0;
	private Double giroZ = 0.0;
	
	public Lectura(){}
	
	public Lectura(Double inclinacion, Integer error, Double aceleracionX,
			Double aceleracionY, Double aceleracionZ, Double temperatura,
			Double giroX, Double giroY, Double giroZ) {
		super();
		
		this.inclinacion = inclinacion;
		this.error = error;
		this.aceleracionX = aceleracionX;
		this.aceleracionY = aceleracionY;
		this.aceleracionZ = aceleracionZ;
		this.temperatura = temperatura;
		this.giroX = giroX;
		this.giroY = giroY;
		this.giroZ = giroZ;
	}
	
	public Double getInclinacion() {
		return inclinacion;
	}
	
	public void setInclinacion(Double inclinacion) {
		this.inclinacion = inclinacion;
	}
	
	public Integer getError() {
		return error;
	}
	
	public void setError(Integer error) {
		this.error = error;
	}
	
	public Double getAceleracionX() {
		return aceleracionX;
	}
	
	public void setAceleracionX(Double aceleracionX) {
		this.aceleracionX = aceleracionX;
	}
	
	public Double getAceleracionY() {
		return aceleracionY;
	}
	
	public void setAceleracionY(Double aceleracionY) {
		this.aceleracionY = aceleracionY;
	}
	
	public Double getAceleracionZ() {
		return aceleracionZ;
	}
	
	public void setAceleracionZ(Double aceleracionZ) {
		this.aceleracionZ = aceleracionZ;
	}
	
	public Double getTemperatura() {
		return temperatura;
	}
	
	public void setTemperatura(Double temperatura) {
		this.temperatura = temperatura;
	}
	
	public Double getGiroX() {
		return giroX;
	}
	
	public void setGiroX(Double giroX) {
		this.giroX = giroX;
	}
	
	public Double getGiroY() {
		return giroY;
	}
	
	public void setGiroY(Double giroY) {
		this.giroY = giroY;
	}
	
	public Double getGiroZ() {
		return giroZ;
	}
	
	public void setGiroZ(Double giroZ) {
		this.giroZ = giroZ;
	}

	@Override
	public String toString() {
		return "Lectura [inclinacion=" + inclinacion + ", error=" + error
				+ ", aceleracionX=" + aceleracionX + ", aceleracionY="
				+ aceleracionY + ", aceleracionZ=" + aceleracionZ
				+ ", temperatura=" + temperatura + ", giroX=" + giroX
				+ ", giroY=" + giroY + ", giroZ=" + giroZ + "]";
	}
	
}