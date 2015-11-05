package ar.com.untref.adquisicion.arduino;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.comm.CommPortIdentifier;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;

import ar.com.untref.adquisicion.arduino.entidades.Lectura;
import ar.com.untref.adquisicion.arduino.entidades.Medicion;
import ar.com.untref.adquisicion.arduino.entidades.Punto3D;

public class Medidor implements SerialPortEventListener {

	private SerialPort serialPort;
	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { "COM13", // Windows
	};

	private Double MARGEN_ERROR_INCLINACION = 3.0;
	private Double MARGEN_ERROR_TEMPERATURA = 1.0;

	private final Double SEGUNDOS_ENTRE_MEDICIONES = 0.5;

	private Double MARGEN_ERROR_ACELERACION_X;
	private Double MARGEN_ERROR_ACELERACION_Y;
	private Double MARGEN_ERROR_ACELERACION_Z;

	private Double VALOR_ACELERACION_X_EN_REPOSO;
	private Double VALOR_ACELERACION_Y_EN_REPOSO;
	private Double VALOR_ACELERACION_Z_EN_REPOSO;

	private Lectura lecturaAnterior = new Lectura();

	private List<Double> valoresDeCalibracionAceleracionX = new ArrayList<Double>();
	private List<Double> valoresDeCalibracionAceleracionY = new ArrayList<Double>();
	private List<Double> valoresDeCalibracionAceleracionZ = new ArrayList<Double>();

	private boolean estaCalibrado = false;

	private Medicion medicionAnterior;

	/**
	 * A BufferedReader which will be fed by a InputStreamReader converting the
	 * bytes into characters making the displayed results codepage independent
	 */
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	private VentanaPrincipal ventanaPrincipal;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	private List<Lectura> bufferLecturas = new ArrayList<Lectura>();
	
	public Medidor(VentanaPrincipal ventanaPrincipal) {

		this.ventanaPrincipal = ventanaPrincipal;
	}

	public void configure() {
		// the next line is for Raspberry Pi and
		// gets us into the while loop and was suggested here was suggested
		// http://www.raspberrypi.org/phpBB3/viewtopic.php?f=81&t=32186
		// System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM1");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(
					serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port. This will prevent
	 * port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {

		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine = input.readLine();

				leerDatos(inputLine);

			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

	private void leerDatos(String inputLine) {

		String[] parametros = inputLine.split("\\|");

		Double inclinacion = new Double(parametros[0]);
		Integer error = new Integer(parametros[1]);
		Double aceleracionX = obtenerAceleracionReal(parametros[2]);
		Double aceleracionY = obtenerAceleracionReal(parametros[3]);
		Double aceleracionZ = obtenerAceleracionReal(parametros[4]);
		Double temperatura = new Double(parametros[5]);
		Double giroX = new Double(parametros[6]);
		Double giroY = new Double(parametros[7]);
		Double giroZ = new Double(parametros[8]);

		Lectura lecturaActual = new Lectura(inclinacion, error, aceleracionX,
				aceleracionY, aceleracionZ, temperatura, giroX, giroY, giroZ);

//		System.out.println(lecturaActual);

		if ( lecturaActual.getError() == 0 ){
			
			if (valoresDeCalibracionAceleracionX.size() <= 20) {

				valoresDeCalibracionAceleracionX.add(lecturaActual
						.getAceleracionX());
				valoresDeCalibracionAceleracionY.add(lecturaActual
						.getAceleracionY());
				valoresDeCalibracionAceleracionZ.add(lecturaActual
						.getAceleracionZ());

			} else if (!estaCalibrado) {

				System.out.println("Calibrando....");
				
				// Ordeno los valores y me quedo con los extremos
				Collections.sort(valoresDeCalibracionAceleracionX);
				Collections.sort(valoresDeCalibracionAceleracionY);
				Collections.sort(valoresDeCalibracionAceleracionZ);

				MARGEN_ERROR_ACELERACION_X = Math
						.abs(valoresDeCalibracionAceleracionX.get(0)
								- valoresDeCalibracionAceleracionX.get(19));
				MARGEN_ERROR_ACELERACION_Y = Math
						.abs(valoresDeCalibracionAceleracionY.get(0)
								- valoresDeCalibracionAceleracionY.get(19));
				MARGEN_ERROR_ACELERACION_Z = Math
						.abs(valoresDeCalibracionAceleracionZ.get(0)
								- valoresDeCalibracionAceleracionZ.get(19));

				VALOR_ACELERACION_X_EN_REPOSO = valoresDeCalibracionAceleracionX
						.get(19) - (MARGEN_ERROR_ACELERACION_X / 2);
				VALOR_ACELERACION_Y_EN_REPOSO = valoresDeCalibracionAceleracionY
						.get(19) - (MARGEN_ERROR_ACELERACION_Y / 2);
				VALOR_ACELERACION_Z_EN_REPOSO = valoresDeCalibracionAceleracionZ
						.get(19) - (MARGEN_ERROR_ACELERACION_Z / 2);

				estaCalibrado = true;

				System.err.println("Fin del proceso de calibracion");
				System.err.println("Error X " + MARGEN_ERROR_ACELERACION_X);
				System.err.println("Valor X " + VALOR_ACELERACION_X_EN_REPOSO);
				System.err.println("Error Y " + MARGEN_ERROR_ACELERACION_Y);
				System.err.println("Valor Y " + VALOR_ACELERACION_Y_EN_REPOSO);
			}

			if (Math.abs(inclinacion - lecturaAnterior.getInclinacion()) < MARGEN_ERROR_INCLINACION) {

				lecturaActual.setInclinacion(lecturaAnterior.getInclinacion());
			}

			if (Math.abs(temperatura - lecturaAnterior.getTemperatura()) < MARGEN_ERROR_TEMPERATURA) {

				lecturaActual.setTemperatura(lecturaAnterior.getTemperatura());
			}

			// if (Math.abs(aceleracionX - lecturaAnterior.getAceleracionX()) <
			// MARGEN_ERROR_ACELERACION_X) {
			//
			// lecturaActual.setAceleracionX(lecturaAnterior.getAceleracionX());
			// }
			//
			// if (Math.abs(aceleracionY - lecturaAnterior.getAceleracionY()) <
			// MARGEN_ERROR_ACELERACION_Y) {
			//
			// lecturaActual.setAceleracionY(lecturaAnterior.getAceleracionY());
			// }
			//
			// if (Math.abs(aceleracionZ - lecturaAnterior.getAceleracionZ()) <
			// MARGEN_ERROR_ACELERACION_X) {
			//
			// lecturaActual.setAceleracionZ(lecturaAnterior.getAceleracionZ());
			// }

			if (estaCalibrado) {
				
				if(bufferLecturas.size() < 5){
					bufferLecturas.add(lecturaActual);
				} else {
					lecturaActual = obtenerLecturaEficaz();
				
					Medicion medicionActual = obtenerMedicion(lecturaActual);
					
					ventanaPrincipal.actualizarDatosBrujula(lecturaActual.getInclinacion());
					ventanaPrincipal.actualizarDatosTemperatura(lecturaActual
							.getTemperatura());
					ventanaPrincipal.actualizarDatosPosicion(medicionActual.getPosicion());
	
					lecturaAnterior = lecturaActual;
					medicionAnterior = medicionActual;
	
					bufferLecturas.clear();
				}
			}
		}
		
	}

	private Lectura obtenerLecturaEficaz() {
		Collections.sort(bufferLecturas, new AceleracionXComparator());
		Lectura lecturaEficaz = bufferLecturas.get(2);
		
		Collections.sort(bufferLecturas, new AceleracionYComparator());
		lecturaEficaz.setAceleracionY(bufferLecturas.get(2).getAceleracionY());
		
		Collections.sort(bufferLecturas, new AceleracionZComparator());
		lecturaEficaz.setAceleracionZ(bufferLecturas.get(2).getAceleracionZ());
		
		return lecturaEficaz;
	}

	private double obtenerAceleracionReal(String parametro) {
		return new Double(parametro) / 16384;
	}

	public void iniciar() throws Exception {

		configure();
		Thread t = new Thread() {
			public void run() {
				// the following line will keep this app alive for 1000 seconds,
				// waiting for events to occur and responding to them (printing
				// incoming messages to console).
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
				}
			}
		};
		t.start();
		System.out.println("Started");
	}

	private Medicion obtenerMedicion(Lectura lectura) {

		Medicion medicionActual = new Medicion(new Punto3D(), new Punto3D(), new Punto3D());

		if (medicionAnterior != null) {

			medicionActual = calcularMedicion(medicionAnterior, lectura);
		}

		return medicionActual;
	}

	private Medicion calcularMedicion(Medicion medicionAnterior,
			Lectura lecturaActual) {

		// Estado de reposo
		Double velocidadAnteriorX = medicionAnterior.getVelocidad().getX();
		Double posicionAnteriorX = medicionAnterior.getPosicion().getX();

		Double velocidadAnteriorY = medicionAnterior.getVelocidad().getY();
		Double posicionAnteriorY = medicionAnterior.getPosicion().getY();

		Double velocidadAnteriorZ = medicionAnterior.getVelocidad().getZ();
		Double posicionAnteriorZ = medicionAnterior.getPosicion().getZ();

		Double posicionActualX = posicionAnteriorX;
		Double velocidadActualX = 0D;

		Double posicionActualY = posicionAnteriorY;
		Double velocidadActualY = 0D;

		Double posicionActualZ = posicionAnteriorZ;
		Double velocidadActualZ = 0D;
		
		Double aceleracionAnteriorX = medicionAnterior.getAceleracion().getX();
		Double aceleracionAnteriorY = medicionAnterior.getAceleracion().getY();
		Double aceleracionAnteriorZ = medicionAnterior.getAceleracion().getZ();

		if (Math.abs(lecturaActual.getAceleracionX()
				- VALOR_ACELERACION_X_EN_REPOSO) > MARGEN_ERROR_ACELERACION_X 
				|| Math.abs(aceleracionAnteriorX
						- VALOR_ACELERACION_X_EN_REPOSO) > MARGEN_ERROR_ACELERACION_X ) {

			velocidadActualX = velocidadAnteriorX
					+ lecturaActual.getAceleracionX() * 1000
					* SEGUNDOS_ENTRE_MEDICIONES;
			posicionActualX = posicionAnteriorX + velocidadActualX
					* SEGUNDOS_ENTRE_MEDICIONES
					+ (lecturaActual.getAceleracionX() / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);

			System.out.println("velocidad X: " + velocidadActualX);
			System.out.println("aceleracion X: "
					+ lecturaActual.getAceleracionX());
			System.out.println("pos actual X: " + posicionActualX);
		}

		if (Math.abs(lecturaActual.getAceleracionY()
				- VALOR_ACELERACION_Y_EN_REPOSO) > MARGEN_ERROR_ACELERACION_Y
				|| Math.abs(aceleracionAnteriorY
						- VALOR_ACELERACION_Y_EN_REPOSO) > MARGEN_ERROR_ACELERACION_Y ) {

			velocidadActualY = velocidadAnteriorY
					+ lecturaActual.getAceleracionY() * 1000
					* SEGUNDOS_ENTRE_MEDICIONES;
			posicionActualY = posicionAnteriorY + velocidadActualY
					* SEGUNDOS_ENTRE_MEDICIONES
					+ (lecturaActual.getAceleracionY() / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);

			System.out.println("velocidad Y: " + velocidadActualY);
			System.out.println("aceleracion Y: "
					+ lecturaActual.getAceleracionY());
			System.out.println("pos actual Y: " + posicionActualY);
		}

		if (Math.abs(lecturaActual.getAceleracionZ()
				- VALOR_ACELERACION_Z_EN_REPOSO) > MARGEN_ERROR_ACELERACION_Z
				|| Math.abs(aceleracionAnteriorZ
						- VALOR_ACELERACION_Z_EN_REPOSO) > MARGEN_ERROR_ACELERACION_Z ) {

			velocidadActualZ = velocidadAnteriorZ
					+ lecturaActual.getAceleracionZ() * 1000
					* SEGUNDOS_ENTRE_MEDICIONES;
			posicionActualZ = posicionAnteriorZ + velocidadActualZ
					* SEGUNDOS_ENTRE_MEDICIONES
					+ (lecturaActual.getAceleracionZ() / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);

			System.out.println("velocidad Z: " + velocidadActualZ);
			System.out.println("aceleracion Z: "
					+ lecturaActual.getAceleracionZ());
			System.out.println("pos actual Z: " + posicionActualZ);
		}

		Punto3D posicionActual = new Punto3D(posicionActualX, posicionActualY,
				posicionActualZ);
		Punto3D velocidadActual = new Punto3D(velocidadActualX,
				velocidadActualY, velocidadActualZ);
		Punto3D aceleracionActual = new Punto3D(lecturaActual.getAceleracionX(),
				lecturaActual.getAceleracionY(), lecturaActual.getAceleracionZ());

		return new Medicion(posicionActual, velocidadActual, aceleracionActual);
	}
	
public class AceleracionXComparator implements Comparator<Lectura> {

	public int compare(Lectura lectura1, Lectura lectura2) {
		return lectura1.getAceleracionX().compareTo(lectura2.getAceleracionX());
	}
	
}

public class AceleracionYComparator implements Comparator<Lectura> {

	public int compare(Lectura lectura1, Lectura lectura2) {
		return lectura1.getAceleracionY().compareTo(lectura2.getAceleracionY());
	}
	
}

public class AceleracionZComparator implements Comparator<Lectura> {

	public int compare(Lectura lectura1, Lectura lectura2) {
		return lectura1.getAceleracionZ().compareTo(lectura2.getAceleracionZ());
	}
	
}
}