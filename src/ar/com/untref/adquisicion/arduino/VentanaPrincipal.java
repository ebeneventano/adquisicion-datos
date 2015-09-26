package ar.com.untref.adquisicion.arduino;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.JTextField;
import javax.swing.JProgressBar;

import ar.com.untref.adquisicion.arduino.utils.GraficadorTermometro;

@SuppressWarnings("serial")
public class VentanaPrincipal extends JFrame {
	
	private JLabel labelBrujula;
	private JTextField textFieldInclinacionNorte;
	private JLabel labelOeste;
	private JLabel labelEste;
	private JLabel labelNorte;
	private JLabel labelSur;
	private JLabel labelt;
	private JLabel labelTemperatura;
	private JProgressBar progressBarTemperatura;
	private int MAXIMO_TEMPERATURA = 60;

	public VentanaPrincipal() {
		
		getContentPane().setLayout(null);
		setTitle("Sistema de Control");
		
		JLabel brujula = new JLabel("Br\u00FAjula");
		brujula.setBackground(Color.WHITE);
		brujula.setHorizontalAlignment(SwingConstants.CENTER);
		brujula.setFont(new Font("Tahoma", Font.PLAIN, 14));
		brujula.setBounds(10, 11, 99, 36);
		getContentPane().add(brujula);
		
		labelBrujula = new JLabel("");
		labelBrujula.setHorizontalAlignment(SwingConstants.CENTER);
		labelBrujula.setFont(new Font("Tahoma", Font.BOLD, 16));
		labelBrujula.setBackground(Color.WHITE);
		labelBrujula.setBounds(10, 58, 99, 36);
		getContentPane().add(labelBrujula);
		
		JLabel labelInc = new JLabel("Inclinaci\u00F3n Norte");
		labelInc.setHorizontalAlignment(SwingConstants.CENTER);
		labelInc.setFont(new Font("Tahoma", Font.PLAIN, 12));
		labelInc.setBackground(Color.WHITE);
		labelInc.setBounds(115, 11, 99, 36);
		getContentPane().add(labelInc);
		
		textFieldInclinacionNorte = new JTextField();
		textFieldInclinacionNorte.setText("0");
		textFieldInclinacionNorte.setHorizontalAlignment(SwingConstants.CENTER);
		textFieldInclinacionNorte.setFont(new Font("Tahoma", Font.PLAIN, 13));
		textFieldInclinacionNorte.setBounds(119, 58, 95, 36);
		getContentPane().add(textFieldInclinacionNorte);
		textFieldInclinacionNorte.setColumns(10);
		
		labelEste = new JLabel("E");
		labelEste.setHorizontalAlignment(SwingConstants.CENTER);
		labelEste.setFont(new Font("Tahoma", Font.PLAIN, 13));
		labelEste.setBounds(69, 187, 31, 26);
		getContentPane().add(labelEste);
		
		labelOeste = new JLabel("O");
		labelOeste.setHorizontalAlignment(SwingConstants.CENTER);
		labelOeste.setFont(new Font("Tahoma", Font.PLAIN, 13));
		labelOeste.setBounds(10, 187, 31, 26);
		getContentPane().add(labelOeste);
		
		labelSur = new JLabel("S");
		labelSur.setHorizontalAlignment(SwingConstants.CENTER);
		labelSur.setFont(new Font("Tahoma", Font.PLAIN, 13));
		labelSur.setBounds(40, 225, 31, 26);
		getContentPane().add(labelSur);
		
		labelNorte = new JLabel("N");
		labelNorte.setHorizontalAlignment(SwingConstants.CENTER);
		labelNorte.setFont(new Font("Tahoma", Font.PLAIN, 13));
		labelNorte.setBounds(40, 145, 31, 26);
		getContentPane().add(labelNorte);
		
		labelt = new JLabel("Temperatura");
		labelt.setHorizontalAlignment(SwingConstants.CENTER);
		labelt.setFont(new Font("Tahoma", Font.PLAIN, 14));
		labelt.setBackground(Color.WHITE);
		labelt.setBounds(325, 11, 99, 36);
		getContentPane().add(labelt);
		
		labelTemperatura = new JLabel("");
		labelTemperatura.setHorizontalAlignment(SwingConstants.CENTER);
		labelTemperatura.setFont(new Font("Tahoma", Font.BOLD, 16));
		labelTemperatura.setBackground(Color.WHITE);
		labelTemperatura.setBounds(325, 58, 99, 36);
		getContentPane().add(labelTemperatura);
		
		progressBarTemperatura = new JProgressBar(0, MAXIMO_TEMPERATURA);
		progressBarTemperatura.setValue(0);
		progressBarTemperatura.setBounds(335, 105, 89, 14);
		getContentPane().add(progressBarTemperatura);
		
		tomarMedidas();
	}

	private void tomarMedidas() {

		SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>(){
	         @Override
	         protected Void doInBackground() throws Exception {

	        	Medidor medidor = new Medidor(VentanaPrincipal.this);
	        	medidor.iniciar();
				
				return null;
	         }
	      };

	      mySwingWorker.execute();
	}
	
	/**
	 * Se calibra la brujula considerando el cero en el norte y aumentando los angulos 
	 * en sentido de las agujas del reloj.
	 * Si el angulo tomado por el sensor es menor al valor referencia norte, debemos hacer
	 * 360 - esa diferencia para reubicar el angulo adecuadamente.
	 * @param angulo
	 */
	public void actualizarDatosBrujula(Double angulo){
		
		Double inclinacionNorte = Double.valueOf(textFieldInclinacionNorte.getText().toString());
		Double diferenciaDeAngulos = angulo - inclinacionNorte;
		if ( diferenciaDeAngulos >= 0) {
			
			angulo = diferenciaDeAngulos;
		} else {
			
			angulo = 360.0 + diferenciaDeAngulos;
		}
		
		labelNorte.setFont(labelNorte.getFont().deriveFont(Font.PLAIN));
		labelSur.setFont(labelSur.getFont().deriveFont(Font.PLAIN));
		labelOeste.setFont(labelOeste.getFont().deriveFont(Font.PLAIN));
		labelEste.setFont(labelEste.getFont().deriveFont(Font.PLAIN));
		
		if ( angulo > 0 && angulo <= 45 || angulo >= 315 && angulo <= 360 ) {
			
			labelNorte.setFont(labelNorte.getFont().deriveFont(Font.BOLD));
		} else if ( angulo <= 225 && angulo >= 135 ) {
			
			labelSur.setFont(labelSur.getFont().deriveFont(Font.BOLD));
		} else if ( angulo <= 135 && angulo >= 45 ) {
			
			labelEste.setFont(labelEste.getFont().deriveFont(Font.BOLD));
		} else {
			
			labelOeste.setFont(labelOeste.getFont().deriveFont(Font.BOLD));
		}
		
		labelBrujula.setText(angulo.toString());
	}

	void actualizarDatosTemperatura(Double temperatura) {

		progressBarTemperatura.setForeground(GraficadorTermometro.getColorTermometro(temperatura, MAXIMO_TEMPERATURA));
		progressBarTemperatura.setValue(temperatura.intValue());
		labelTemperatura.setText(temperatura.toString());
	}
}
