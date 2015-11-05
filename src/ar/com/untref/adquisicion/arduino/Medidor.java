package ar.com.untref.adquisicion.arduino;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

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

	private final Double SEGUNDOS_ENTRE_MEDICIONES = 0.1;
	private Double MARGEN_ERROR_ACELERACION = 0.7;

	private Lectura lecturaAnterior = new Lectura();

	private Medicion medicionAnterior;
	private boolean hayCambioEnAceleracionX;
	private boolean hayCambioEnAceleracionY;
	private boolean hayCambioEnAceleracionZ;

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
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other
		// ones.
	}

	private void leerDatos(String inputLine) {

		hayCambioEnAceleracionX = true;
		hayCambioEnAceleracionY = true;
		hayCambioEnAceleracionZ = true;

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

		Double anguloY = Math
				.toDegrees(Math.atan(aceleracionX
						/ (Math.sqrt(((aceleracionY * aceleracionY) + (aceleracionZ * aceleracionZ))))));

		System.out.println("angulo Y = " + anguloY);

		Double anguloX = Math
				.toDegrees(Math.atan(aceleracionY
						/ (Math.sqrt(((aceleracionX * aceleracionX) + (aceleracionZ * aceleracionZ))))));

		System.out.println("angulo X  = " + anguloX);

		Lectura lecturaActual = new Lectura(inclinacion, error, aceleracionX,
				aceleracionY, aceleracionZ, temperatura, giroX, giroY, giroZ);

		System.out.println(lecturaActual);

		if (Math.abs(inclinacion - lecturaAnterior.getInclinacion()) < MARGEN_ERROR_INCLINACION) {

			lecturaActual.setInclinacion(lecturaAnterior.getInclinacion());
		}

		if (Math.abs(temperatura - lecturaAnterior.getTemperatura()) < MARGEN_ERROR_TEMPERATURA) {

			lecturaActual.setTemperatura(lecturaAnterior.getTemperatura());
		}

		if (Math.abs(aceleracionX - lecturaAnterior.getAceleracionX()) < MARGEN_ERROR_ACELERACION) {

			hayCambioEnAceleracionX = false;
			lecturaActual.setAceleracionX(lecturaAnterior.getAceleracionX());
		}

		if (Math.abs(aceleracionY - lecturaAnterior.getAceleracionY()) < MARGEN_ERROR_ACELERACION) {
			
			hayCambioEnAceleracionY = false;
			lecturaActual.setAceleracionY(lecturaAnterior.getAceleracionY());
		}

		if (Math.abs(aceleracionZ - lecturaAnterior.getAceleracionZ()) < MARGEN_ERROR_ACELERACION) {

			hayCambioEnAceleracionZ = false;
			lecturaActual.setAceleracionZ(lecturaAnterior.getAceleracionZ());
		}

		Medicion medicionActual = obtenerMedicion(lecturaActual);

		ventanaPrincipal.actualizarDatosBrujula(lecturaActual.getInclinacion());
		ventanaPrincipal.actualizarDatosTemperatura(lecturaActual
				.getTemperatura());
		ventanaPrincipal.actualizarDatosPosicion(medicionActual.getPosicion());

		lecturaAnterior = lecturaActual;
		medicionAnterior = medicionActual;
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

		Medicion medicionActual = new Medicion(new Punto3D(), new Punto3D());

		if (medicionAnterior != null) {

			medicionActual = calcularMedicion(medicionAnterior, lectura);
		}

		return medicionActual;
	}

	private Medicion calcularMedicion(Medicion medicionAnterior,
			Lectura lecturaActual) {

		Double velocidadActualX = medicionAnterior.getVelocidad().getX();
		Double posicionActualX = medicionAnterior.getPosicion().getX();

		Double velocidadActualY = medicionAnterior.getVelocidad().getY();
		Double posicionActualY = medicionAnterior.getPosicion().getY();

		Double velocidadActualZ = medicionAnterior.getVelocidad().getZ();
		Double posicionActualZ = medicionAnterior.getPosicion().getZ();

		if (hayCambioEnAceleracionX && lecturaActual.getAceleracionX() > 1.0D) {
			velocidadActualX = 0D;
			posicionActualX = 0D;
			velocidadActualX += lecturaActual.getAceleracionX() * 1000
					* SEGUNDOS_ENTRE_MEDICIONES;
			posicionActualX += velocidadActualX * SEGUNDOS_ENTRE_MEDICIONES
					+ (lecturaActual.getAceleracionX() / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);

			System.out.println("velocidad X: " + velocidadActualX);
			System.out.println("aceleracion X: " + lecturaActual.getAceleracionX());
			System.out.println("pos actual X: " + posicionActualX);
		}

		if (hayCambioEnAceleracionY && lecturaActual.getAceleracionY() > 1.0D) {
			velocidadActualY = 0D;
			posicionActualY = 0D;
			velocidadActualY += lecturaActual.getAceleracionY() * 1000
					* SEGUNDOS_ENTRE_MEDICIONES;
			posicionActualY += velocidadActualY * SEGUNDOS_ENTRE_MEDICIONES
					+ (lecturaActual.getAceleracionY() / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);

			System.out.println("velocidad Y: " + velocidadActualY);
			System.out.println("aceleracion Y: " + lecturaActual.getAceleracionY());
			System.out.println("pos actual Y: " + posicionActualY);
		}

		if (hayCambioEnAceleracionZ && lecturaActual.getAceleracionZ() > 1.0D) {
			velocidadActualZ = 0D;
			posicionActualZ = 0D;
			velocidadActualZ += lecturaActual.getAceleracionZ() * 1000
					* SEGUNDOS_ENTRE_MEDICIONES;
			posicionActualZ += velocidadActualZ * SEGUNDOS_ENTRE_MEDICIONES
					+ (lecturaActual.getAceleracionZ() / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);

			System.out.println("velocidad Z: " + velocidadActualZ);
			System.out.println("aceleracion Z: " + lecturaActual.getAceleracionZ());
			System.out.println("pos actual Z: " + posicionActualZ);
		}

		Punto3D posicionActual = new Punto3D(posicionActualX, posicionActualY,
				posicionActualZ);
		Punto3D velocidadActual = new Punto3D(velocidadActualX,
				velocidadActualY, velocidadActualZ);

		return new Medicion(posicionActual, velocidadActual);
	}
}