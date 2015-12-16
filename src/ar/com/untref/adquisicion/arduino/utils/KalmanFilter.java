package ar.com.untref.adquisicion.arduino.utils;

import java.math.BigDecimal;
import java.util.List;

import ar.com.untref.adquisicion.arduino.entidades.Lectura;

public class KalmanFilter {
	private double estimacionAnterior;
	private double covarianzaErrorEstimacionAnterior;
	private double estimacionActual;
	private double covarianzaErrorEstimacionActual;
	private double desviacionErrorMedicion;
	private double errorDelProceso;
	
	/**
	 * El filtro se inicializa con dos variables que deben conocerse con anterioridad para
	 * su correcto funcionamiento: Desviaci�n del error de la medici�n y Desviaci�n del error
	 * del proceso. El filtro se inicializa con los valores de estimacion y covarianza anterior
	 * en 0 y 1 respectivamente.
	 * @param desviacionErrorMedicion
	 * @param errorDelProceso
	 */
	public KalmanFilter(double desviacionErrorMedicion, double errorDelProceso){
		
		this.setDesviacionErrorMedicion(desviacionErrorMedicion);
		this.errorDelProceso = errorDelProceso;
		inicializar();
	}
	
	private void inicializar() {

		estimacionAnterior = 0;
		covarianzaErrorEstimacionAnterior = 1;
	}

	/**
	 * Aplica los m�todos de Predicci�n y Correcci�n a la lectura recibida.
	 * @param lecturaActual
	 * @return valor de lectura estimado
	 */
	public double aplicarKalman(Lectura lecturaActual){
		
		calcularPrediccion(lecturaActual);
		calcularCorreccion(lecturaActual);
		
		//La estimaci�n anterior ya se actualiz� con los valores calculados
		return new BigDecimal(estimacionAnterior).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * Etapa de predicci�n
	 * Setea los valores de estimacion y covarianza del error para la iteraci�n actual.
	 * @param lecturaActual
	 */
	private void calcularPrediccion(Lectura lecturaActual) {
		
		estimacionActual = estimacionAnterior;
		covarianzaErrorEstimacionActual = covarianzaErrorEstimacionAnterior;
	}
	
	/**
	 * Etapa de correcci�n.
	 * Se calcula la ganancia y se actualizan los valores de estimacion y covarianza del error para 
	 * la pr�xima iteraci�n.
	 * @param lecturaActual
	 */
	private void calcularCorreccion(Lectura lecturaActual) {
		double gananciaActual = (double) covarianzaErrorEstimacionActual / (covarianzaErrorEstimacionActual + getDesviacionErrorMedicion());
		
		estimacionAnterior = estimacionActual + gananciaActual * ( lecturaActual.getAceleracionX() - estimacionAnterior );
		covarianzaErrorEstimacionAnterior = ( 1 - gananciaActual ) * covarianzaErrorEstimacionActual + errorDelProceso;
	}
	
	/**
	 * Devuelve la desviaci�n estandar de la poblaci�n enviada por par�metro.
	 * @return desviaci�n estandar poblacional.
	 */
	public static double calcularDesviaci�nEstandar(List<Double> poblacion){
		
		double bufferPromedio = 0;
		double promedio;
		double desviacionEstandar = 0;
		
		for (Double valorActual : poblacion) {
			
			bufferPromedio += valorActual;
		}
		
		promedio = bufferPromedio / poblacion.size();
		
		for (int i=0; i<poblacion.size(); i++) {
			
			desviacionEstandar += Math.pow(poblacion.get(i) - promedio, 2);
		}
		
		desviacionEstandar = Math.sqrt(desviacionEstandar / poblacion.size());
		
		return desviacionEstandar;
	}

	public double getDesviacionErrorMedicion() {
		return desviacionErrorMedicion;
	}

	public void setDesviacionErrorMedicion(double desviacionErrorMedicion) {
		this.desviacionErrorMedicion = desviacionErrorMedicion;
	}
}