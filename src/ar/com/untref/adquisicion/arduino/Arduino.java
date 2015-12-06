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
import ar.com.untref.adquisicion.excepcion.LecturaErroneaException;

public class Arduino implements SerialPortEventListener {

	private SerialPort serialPort;

	private static final String PORT_NAMES[] = { "COM13", // Windows
	};

	private Double MARGEN_ERROR_INCLINACION = 3.0;
	private Double MARGEN_ERROR_TEMPERATURA = 1.0;

	private final Double SEGUNDOS_ENTRE_MEDICIONES = 0.1;

	private Lectura lecturaAnterior = new Lectura();

	private List<Double> listaDeLecturas = new ArrayList<Double>();

	private Medicion medicionAnterior;

	private BufferedReader input;
	private OutputStream output;
	private VentanaPrincipal ventanaPrincipal;

	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 9600;

	private List<Lectura> bufferLecturas = new ArrayList<Lectura>();
	private Calibrador calibrador;

	public Arduino(VentanaPrincipal ventanaPrincipal) {
		this.ventanaPrincipal = ventanaPrincipal;
		inicializar();
	}

	public void inicializar() {
		this.configure();
		this.calibrador = new Calibrador();
		
		Thread t = new Thread() {
			public void run() {
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
					System.err.println(ie.toString());
				}
			}
		};
		t.start();
		System.out.println("Started");
	}

	public void configure() {
		CommPortIdentifier portId = obtenerPortId();
		
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			input = new BufferedReader(new InputStreamReader(
					serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	private CommPortIdentifier obtenerPortId() {
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId = null;
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		
		return portId;
	}

	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (puertoSerialEstaDisponible(oEvent)) {
			try {
				this.sensar(input.readLine());
			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
	}

	private boolean puertoSerialEstaDisponible(SerialPortEvent oEvent) {
		return oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE;
	}

	private void sensar(String inputLine) {
		try {
			Lectura lecturaActual = new LecturaFactory().crear(inputLine);
			
			Boolean calibrado = this.calibrador.calibrar(lecturaActual);
			
			this.setearInclinacion(lecturaActual);
			this.setearTemperatura(lecturaActual);
			
			if (calibrado) {
				listaDeLecturas.add(lecturaActual.getAceleracionY());
				
				if (listaDeLecturas.size() == 50) {
					for (Double lect : listaDeLecturas) {
						System.out.println(lect.toString());
					}
				}
				
				Medicion medicionActual = obtenerMedicion(lecturaActual);
				
				ventanaPrincipal.actualizarDatosBrujula(lecturaActual
						.getInclinacion());
				ventanaPrincipal.actualizarDatosTemperatura(lecturaActual
						.getTemperatura());
				ventanaPrincipal.actualizarDatosPosicion(medicionActual
						.getPosicion());
				
				lecturaAnterior = lecturaActual;
				medicionAnterior = medicionActual;
			}
		} catch (LecturaErroneaException e) {
			System.err.println(e.toString());
		}
	}

	private void setearTemperatura(Lectura lecturaActual) {
		if (Math.abs(lecturaActual.getTemperatura()
				- lecturaAnterior.getTemperatura()) < MARGEN_ERROR_TEMPERATURA) {
			
			lecturaActual.setTemperatura(lecturaAnterior.getTemperatura());
		}
	}

	private void setearInclinacion(Lectura lecturaActual) {
		if (Math.abs(lecturaActual.getInclinacion()
				- lecturaAnterior.getInclinacion()) < MARGEN_ERROR_INCLINACION) {
			
			lecturaActual.setInclinacion(lecturaAnterior.getInclinacion());
		}
	}

	private Medicion obtenerMedicion(Lectura lectura) {
		Medicion medicionActual = new Medicion(new Punto3D(), new Punto3D(),
				new Punto3D());

		if (medicionAnterior != null) {
			medicionActual = calcularMedicion(medicionAnterior, lectura);
		}

		return medicionActual;
	}

	private Medicion calcularMedicion(Medicion medicionAnterior,
			Lectura lecturaActual) {

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
				- this.calibrador.getValorAcelaracionXReposo()) > this.calibrador.getMargenErrorAceleracionX()
				&& Math.abs(lecturaActual.getAceleracionX()) >= 2800) {

			velocidadActualX = aceleracionAnteriorX * SEGUNDOS_ENTRE_MEDICIONES
					* SEGUNDOS_ENTRE_MEDICIONES * 1000 / 16384;
			
			posicionActualX = posicionAnteriorX + velocidadActualX
					+ (lecturaActual.getAceleracionX() * 1000 / 16348 / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);

		}

		if (Math.abs(lecturaActual.getAceleracionY()
				- this.calibrador.getValorAceleracionYReposo()) > this.calibrador.getMargenErrorAceleracionY()
				&& Math.abs(lecturaActual.getAceleracionY()) >= 2800) {

			velocidadActualY = aceleracionAnteriorY * SEGUNDOS_ENTRE_MEDICIONES
					* SEGUNDOS_ENTRE_MEDICIONES * 1000 / 16384;
			
			posicionActualY = posicionAnteriorY + velocidadActualY
					+ (lecturaActual.getAceleracionY() * 1000 / 16348 / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);

		}

		if (Math.abs(lecturaActual.getAceleracionZ()
				- this.calibrador.getValorAceleracionZReposo()) > this.calibrador.getMargenErrorAceleracionZ()
				&& Math.abs(lecturaActual.getAceleracionZ()) >= 2800) {

			velocidadActualZ = aceleracionAnteriorZ * SEGUNDOS_ENTRE_MEDICIONES
					* SEGUNDOS_ENTRE_MEDICIONES * 1000 / 16384;
			
			posicionActualZ = posicionAnteriorZ + velocidadActualZ
					+ (lecturaActual.getAceleracionZ() * 1000 / 16348 / 2)
					* (SEGUNDOS_ENTRE_MEDICIONES * SEGUNDOS_ENTRE_MEDICIONES);
		}

		Punto3D posicionActual = new Punto3D(posicionActualX, posicionActualY,
				posicionActualZ);
		Punto3D velocidadActual = new Punto3D(velocidadActualX,
				velocidadActualY, velocidadActualZ);
		
		Punto3D aceleracionActual = new Punto3D(
				lecturaActual.getAceleracionX(),
				lecturaActual.getAceleracionY(),
				lecturaActual.getAceleracionZ());

		return new Medicion(posicionActual, velocidadActual, aceleracionActual);
	}

	public class AceleracionXComparator implements Comparator<Lectura> {
		public int compare(Lectura lectura1, Lectura lectura2) {
			return lectura1.getAceleracionX().compareTo(
					lectura2.getAceleracionX());
		}
	}

	public class AceleracionYComparator implements Comparator<Lectura> {
		public int compare(Lectura lectura1, Lectura lectura2) {
			return lectura1.getAceleracionY().compareTo(
					lectura2.getAceleracionY());
		}
	}

	public class AceleracionZComparator implements Comparator<Lectura> {
		public int compare(Lectura lectura1, Lectura lectura2) {
			return lectura1.getAceleracionZ().compareTo(
					lectura2.getAceleracionZ());
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
}