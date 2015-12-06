package ar.com.untref.adquisicion.arduino.utils;

import java.awt.Color;

public class GraficadorTermometro {

	/**
	 * Devuelve un color para el termometro considerando temperaturas desde cero hasta @param maximo
	 * @param temperatura
	 * @param maximo
	 * @return
	 */
	public static Color getColorTermometro(double temperatura, double maximo) {
		
		double porcentaje = (temperatura * 100) / maximo;
		Color color = Color.RED;
		
		if ( porcentaje < 20 ) {
			
			color = Color.BLUE;
		} else if ( porcentaje < 40 ) {
			
			color = Color.YELLOW;
		} else if ( porcentaje < 70 ) {
			
			color = Color.ORANGE;
		}
		
		return color;
	}
}
