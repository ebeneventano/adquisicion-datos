package ar.com.untref.adquisicion.arduino;

import javax.swing.JFrame;

public class Main {

	public static void main(String[] args) {

		VentanaPrincipal vp = new VentanaPrincipal();
		vp.setVisible(true);
		vp.setExtendedState(vp.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

}
