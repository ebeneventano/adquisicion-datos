package ar.com.untref.adquisicion.arduino;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ar.com.untref.adquisicion.arduino.entidades.Lectura;
import ar.com.untref.adquisicion.arduino.utils.KalmanFilter;

public class Calibrador {
	
	private Double margenErrorAceleracionX;
	private Double margenErrorAceleracionY;
	private Double margenErrorAceleracionZ;

	private Double valorAcelaracionXReposo;
	private Double valorAceleracionYReposo;
	private Double valorAceleracionZReposo;

	private List<Double> valoresDeCalibracionAceleracionX = new ArrayList<Double>();
	private List<Double> valoresDeCalibracionAceleracionY = new ArrayList<Double>();
	private List<Double> valoresDeCalibracionAceleracionZ = new ArrayList<Double>();
	
	private Boolean calibrado;
	
	private KalmanFilter filter;
	
	public Calibrador() {
		calibrado = Boolean.FALSE;
	}

	public Boolean calibrar(Lectura lecturaActual) {
		
		if (getValoresDeCalibracionAceleracionX().size() <= 20) {
			getValoresDeCalibracionAceleracionX().add(lecturaActual
					.getAceleracionX());
			valoresDeCalibracionAceleracionY.add(lecturaActual
					.getAceleracionY());
			valoresDeCalibracionAceleracionZ.add(lecturaActual
					.getAceleracionZ());
			
		} else if (!calibrado) {
			
			System.out.println("Calibrando....");
			
			// Ordeno los valores y me quedo con los extremos
			Collections.sort(getValoresDeCalibracionAceleracionX());
			Collections.sort(valoresDeCalibracionAceleracionY);
			Collections.sort(valoresDeCalibracionAceleracionZ);
			
			setMargenErrorAceleracionX(Math
					.abs(getValoresDeCalibracionAceleracionX().get(0)
							- getValoresDeCalibracionAceleracionX().get(19)));
			setMargenErrorAceleracionY(Math
					.abs(valoresDeCalibracionAceleracionY.get(0)
							- valoresDeCalibracionAceleracionY.get(19)));
			setMargenErrorAceleracionZ(Math
					.abs(valoresDeCalibracionAceleracionZ.get(0)
							- valoresDeCalibracionAceleracionZ.get(19)));
			
			setValorAcelaracionXReposo(getValoresDeCalibracionAceleracionX()
					.get(19) - (getMargenErrorAceleracionX() / 2));
			setValorAceleracionYReposo(valoresDeCalibracionAceleracionY
					.get(19) - (getMargenErrorAceleracionY() / 2));
			setValorAceleracionZReposo(valoresDeCalibracionAceleracionZ
					.get(19) - (getMargenErrorAceleracionZ() / 2));
			
			double desviacionEstandar = KalmanFilter.calcularDesviaciˇnEstandar(getValoresDeCalibracionAceleracionX());
			System.out.println("desviacion estandar: " + desviacionEstandar);
			setFilter(new KalmanFilter(desviacionEstandar, 0.01));
			
			calibrado = Boolean.TRUE;
		}
		
		return calibrado;
	}

	public Double getMargenErrorAceleracionX() {
		return margenErrorAceleracionX;
	}

	public void setMargenErrorAceleracionX(Double margenErrorAceleracionX) {
		this.margenErrorAceleracionX = margenErrorAceleracionX;
	}

	public Double getMargenErrorAceleracionY() {
		return margenErrorAceleracionY;
	}

	public void setMargenErrorAceleracionY(Double margenErrorAceleracionY) {
		this.margenErrorAceleracionY = margenErrorAceleracionY;
	}

	public Double getMargenErrorAceleracionZ() {
		return margenErrorAceleracionZ;
	}

	public void setMargenErrorAceleracionZ(Double margenErrorAceleracionZ) {
		this.margenErrorAceleracionZ = margenErrorAceleracionZ;
	}

	public Double getValorAcelaracionXReposo() {
		return valorAcelaracionXReposo;
	}

	public void setValorAcelaracionXReposo(Double valorAcelaracionXReposo) {
		this.valorAcelaracionXReposo = valorAcelaracionXReposo;
	}

	public Double getValorAceleracionYReposo() {
		return valorAceleracionYReposo;
	}

	public void setValorAceleracionYReposo(Double valorAceleracionYReposo) {
		this.valorAceleracionYReposo = valorAceleracionYReposo;
	}

	public Double getValorAceleracionZReposo() {
		return valorAceleracionZReposo;
	}

	public void setValorAceleracionZReposo(Double valorAceleracionZReposo) {
		this.valorAceleracionZReposo = valorAceleracionZReposo;
	}

	public List<Double> getValoresDeCalibracionAceleracionX() {
		return valoresDeCalibracionAceleracionX;
	}

	public void setValoresDeCalibracionAceleracionX(
			List<Double> valoresDeCalibracionAceleracionX) {
		this.valoresDeCalibracionAceleracionX = valoresDeCalibracionAceleracionX;
	}

	public KalmanFilter getFilter() {
		return filter;
	}

	public void setFilter(KalmanFilter filter) {
		this.filter = filter;
	}
	
}
