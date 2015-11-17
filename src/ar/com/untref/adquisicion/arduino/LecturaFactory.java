package ar.com.untref.adquisicion.arduino;

import ar.com.untref.adquisicion.arduino.entidades.Lectura;
import ar.com.untref.adquisicion.excepcion.LecturaErroneaException;

public class LecturaFactory {

	private static final Integer VALOR_LECTURA_SIN_ERROR = 0;
	
	private static final String SEPARADOR = "\\|";

	public Lectura crear(String inputLine) throws LecturaErroneaException {
		String[] parametros = inputLine.split(SEPARADOR);

		Double inclinacion = new Double(parametros[0]);
		Integer error = new Integer(parametros[1]);
		Double aceleracionX = new Double(parametros[2]);
		Double aceleracionY = new Double(parametros[3]);
		Double aceleracionZ = new Double(parametros[4]);
		Double temperatura = new Double(parametros[5]);
		Double giroX = new Double(parametros[6]);
		Double giroY = new Double(parametros[7]);
		Double giroZ = new Double(parametros[8]);
		
		validarLectura(error);

		return new Lectura(inclinacion, error, aceleracionX,
				aceleracionY, aceleracionZ, temperatura, giroX, giroY, giroZ);
	}

	private void validarLectura(Integer error) throws LecturaErroneaException {
		if (!error.equals(VALOR_LECTURA_SIN_ERROR)) {
			throw new LecturaErroneaException();
		}
	}
	
}
