package ar.com.untref.adquisicion.arduino;

public class ArduinoFactory {

	public Arduino crear(VentanaPrincipal ventanaPrincipal) {
		return new Arduino(ventanaPrincipal);
	}
	
}