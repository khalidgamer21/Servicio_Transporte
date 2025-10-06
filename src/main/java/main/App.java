package main;

// Importamos las librerías que necesitamos
import javax.swing.*;// Para las ventanas emergentes (JOptionPane, tablas, etc.)
import javax.swing.table.DefaultTableModel; // Para manejar las tablas de pasajeros
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;// Para trabajar con fechas y horas
import java.util.List;
import java.util.Optional;
// Importamos nuestras clases creadas
import modelo.*;
import patron_diseno.*;
import excepciones.AsientosInsuficientes;

public class App {// Clase principal del programa
    public static void main(String[] args) {// metodo main - entrada del programa
        SistemaTransporte sistema = SistemaTransporte.getInstancia(); // obtiene singleton del sistema
        int opcion = 0;// variable para controlar la opción del menú

        // formateadores reutilizables para fecha y hora
        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); //formarto fecha
        DateTimeFormatter horaFormatter  = DateTimeFormatter.ofPattern("HH:mm");// formato hora

        // Aquí hago un bucle para que el menú se repita hasta que el usuario elija salir
        do {
            try {
                // Este es el menú con todas las opciones que se muestran al usuario

                String menu = "*** MENÚ SISTEMA DE TRANSPORTE ***\n" +
                        "1. Crear viaje\n" +
                        "2. Agregar cliente a un viaje (con asiento y tiquete)\n" +
                        "3. Mostrar ingresos de cada viaje\n" +
                        "4. Cancelar reserva\n" +
                        "5. Mostrar estadísticas (Streams)\n" +
                        "6. Buscar cliente por cédula\n" +
                        "7. Mostrar pasajeros de un viaje en tabla (JTable)\n" +
                        "8. Salir\n" +
                        "Seleccione una opción:";
                // Le muestro al usuario el menú y guardo lo que digite
                String input = JOptionPane.showInputDialog(menu);

                // Si el usuario cancela la ventana, aquí termino el programa
                if (input == null) break;
                // Convierto la opción que escribió el usuario en un número entero
                opcion = Integer.parseInt(input.trim());
                // Aquí reviso qué opción eligió el usuario y voy al caso correspondiente
                switch (opcion) {
                    // ----------------- CASE 1: Crear viaje -----------------
                    case 1: {
                        // Le pregunto al usuario que tipo de vehículo quiere para el viaje
                        String tipoStr = JOptionPane.showInputDialog(
                                "*** Crear Viaje ***\n" +
                                        "Seleccione el tipo de vehículo:\n" +
                                        "1. Bus (40 puestos, $50.000)\n" +
                                        "2. Minivan (10 puestos, $20.000)"
                        );
                        // Si cancela aquí, salgo de este case
                        if (tipoStr == null) break;
                        // Convierto lo que escribió en un número
                        int tipoVehiculo = Integer.parseInt(tipoStr.trim());
                        // Si escribió un número que no es 1 ni 2, le aviso que es inválido
                        if (tipoVehiculo != 1 && tipoVehiculo != 2) {
                            JOptionPane.showMessageDialog(null, "Tipo de vehículo no válido.");
                            break;
                        }
                        // Uso la fábrica para crear un Bus o una Minivan según lo que eligió
                        Vehiculo vehiculo = VehiculoFactory.crearVehiculo(tipoVehiculo);
                        // Pido la ciudad de origen
                        String origen = JOptionPane.showInputDialog("Ingrese ciudad de origen:");
                        if (origen == null || origen.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Origen inválido."); break; }
                        // Pido la ciudad de destino
                        String destino = JOptionPane.showInputDialog("Ingrese ciudad de destino:");
                        if (destino == null || destino.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Destino inválido."); break; }

                        // Valido que origen y destino no estén vacíos ni sean iguales
                        if (origen.trim().equalsIgnoreCase(destino.trim())) {
                            JOptionPane.showMessageDialog(null, "Origen y destino no pueden ser iguales.");
                            break;
                        }
                        // Pido la fecha de salida del viaje
                        String fechaStr = JOptionPane.showInputDialog("Ingrese la fecha de salida (yyyy-MM-dd)\nEjemplo: 2025-10-21");
                        if (fechaStr == null || fechaStr.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Fecha inválida."); break; }
                        // Pido la hora de salida del viaje
                        String horaStr = JOptionPane.showInputDialog("Ingrese la hora de salida (HH:mm)\nEjemplo: 14:30");
                        if (horaStr == null || horaStr.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Hora inválida."); break; }

                        LocalDate fecha;
                        LocalTime hora;

                        // Convierto lo que escribió en un objeto de tipo fecha y hora
                        try {
                            fecha = LocalDate.parse(fechaStr.trim(), fechaFormatter);
                            hora = LocalTime.parse(horaStr.trim(), horaFormatter);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Formato de fecha u hora inválido. Use yyyy-MM-dd y HH:mm.");// Junto fecha y hora en un solo objeto
                            break;
                        }
                        LocalDateTime fechaSalida = LocalDateTime.of(fecha, hora);

                        // pedir datos del conductor
                        String nombreConductor = JOptionPane.showInputDialog("Ingrese el nombre del conductor:");
                        if (nombreConductor == null || nombreConductor.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Nombre conductor inválido."); break; }

                        String cedulaConductor = JOptionPane.showInputDialog("Ingrese la cédula del conductor:");
                        if (cedulaConductor == null || cedulaConductor.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Cédula conductor inválida."); break; }

                        String telefonoConductor = JOptionPane.showInputDialog("Ingrese el teléfono del conductor:");
                        if (telefonoConductor == null || telefonoConductor.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Teléfono conductor inválido."); break; }

                        String licenciaConductor = JOptionPane.showInputDialog("Ingrese la licencia del conductor:");
                        if (licenciaConductor == null || licenciaConductor.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Licencia conductor inválida."); break; }
                        // Creo un objeto Conductor con los datos que me dio el usuario
                        Conductor conductor = new Conductor(
                                nombreConductor.trim(),
                                cedulaConductor.trim(),
                                telefonoConductor.trim(),
                                true,// El conductor empieza activo
                                licenciaConductor.trim()
                        );
                        // Creo un nuevo viaje con toda la información
                        Viajes nuevoViaje = new Viajes(vehiculo, origen.trim(), destino.trim(), fechaSalida, conductor);
                        // Agrego el viaje al sistema
                        sistema.agregarViaje(nuevoViaje);
                        // Le muestro al usuario un resumen del viaje que acaba de crear
                        JOptionPane.showMessageDialog(null,
                                "✅ Viaje creado\n" +
                                        "Origen: " + nuevoViaje.getOrigen() + "\n" +
                                        "Destino: " + nuevoViaje.getDestino() + "\n" +
                                        "Fecha: " + nuevoViaje.getFechaSalida() + "\n" +
                                        "Vehículo: " + nuevoViaje.getVehiculo().getClass().getSimpleName() + "\n" +
                                        "Conductor: " + nuevoViaje.getConductor().toString() + "\n" +
                                        "Tarifa por pasajero: $" + nuevoViaje.getVehiculo().calcularTarifa()
                        );
                        break;
                    }

                    // ----------------- CASE 2: Agregar cliente -----------------
                    case 2: {
                        // Primero reviso si hay viajes creados
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados aún."); break; }
                        // Hago un listado con todos los viajes y se lo muestro al usuario
                        StringBuilder listaViajes = new StringBuilder("Viajes disponibles:\n");
                        for (int i = 0; i < sistema.getViajes().size(); i++) {
                            Viajes vi = sistema.getViajes().get(i);
                            listaViajes.append(i).append(". ")
                                    .append(vi.getOrigen()).append(" → ").append(vi.getDestino())
                                    .append(" | Conductor: ").append(vi.getConductor().getNombre())
                                    .append(" | Fecha: ").append(vi.getFechaSalida())
                                    .append(" | Pasajeros: ").append(vi.getClientes().size())
                                    .append("/").append(vi.getVehiculo().getCapacidad())
                                    .append("\n");
                        }
                        // El usuario selecciona el número de viaje en el que quiere reservar
                        String sel = JOptionPane.showInputDialog(listaViajes.toString() + "\nSeleccione el número de viaje:");
                        if (sel == null) break;// si cancela, salgo
                        int index = Integer.parseInt(sel.trim());// convierto a número
                        if (index < 0 || index >= sistema.getViajes().size()) { JOptionPane.showMessageDialog(null, "Número de viaje no válido."); break; }// si está fuera de rango, salgo
                        // Tomo el viaje que eligió el usuario
                        Viajes viaje = sistema.getViajes().get(index);

                        // Pido los datos del cliente

                        String nombreCliente = JOptionPane.showInputDialog("Ingrese el nombre del cliente:");
                        if (nombreCliente == null || nombreCliente.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Nombre inválido."); break; }

                        String cedulaCliente = JOptionPane.showInputDialog("Ingrese la cédula del cliente:");
                        if (cedulaCliente == null || cedulaCliente.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Cédula inválida."); break; }

                        String telefonoCliente = JOptionPane.showInputDialog("Ingrese el teléfono del cliente:");
                        if (telefonoCliente == null || telefonoCliente.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Teléfono inválido."); break; }
                        // Ahora pido el asiento y lo valido en un bucle
                        int asiento = -1;
                        boolean asientoValido = false;

                        while (!asientoValido) {
                            String asientoStr = JOptionPane.showInputDialog("Ingrese el número de asiento (1 - " + viaje.getVehiculo().getCapacidad() + "):");
                            if (asientoStr == null || asientoStr.trim().isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Debe ingresar un número de asiento.");
                                continue; // vuelve a preguntar
                            }

                            try {
                                asiento = Integer.parseInt(asientoStr.trim());
                                if (asiento > 0 && asiento <= viaje.getVehiculo().getCapacidad()) {
                                    asientoValido = true; // si está en el rango correcto, salgo del bucle
                                } else {
                                    JOptionPane.showMessageDialog(null, "Número de asiento fuera de rango. Intente de nuevo.");
                                }
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, "Debe ingresar un número válido.");
                            }
                        }




                        String correoCliente = JOptionPane.showInputDialog("Ingrese el correo del cliente:");
                        if (correoCliente == null || correoCliente.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Correo inválido."); break; }
                        // Creo un objeto Cliente con la información que el usuario me dio
                        Cliente cliente = new Cliente(
                                nombreCliente.trim(),
                                cedulaCliente.trim(),
                                telefonoCliente.trim(),
                                correoCliente.trim(),
                                asiento
                        );
                        try {
                            // Intento agregar el cliente al viaje

                            viaje.agregarCliente(cliente);
                            // Si todo sale bien, muestro el tiquete con los datos
                            JOptionPane.showMessageDialog(null,
                                    "Tiquete generado:\n" +
                                            "Cliente: " + cliente.toString() + "\n"  +
                                            "Correo: " + cliente.getCorreo() + "\n" +
                                            "Viaje: " + viaje.getOrigen() + " → " + viaje.getDestino() + "\n" +
                                            "Fecha: " + viaje.getFechaSalida() + "\n" +
                                            "Vehículo: " + viaje.getVehiculo().getClass().getSimpleName() + "\n" +
                                            "Conductor: " + viaje.getConductor().toString() + "\n" +
                                            "Precio: $" + viaje.getVehiculo().calcularTarifa()
                            );
                        } catch (AsientosInsuficientes aie) {
                            // Si ya no hay asientos, muestro este error
                            JOptionPane.showMessageDialog(null, aie.getMessage());
                        } catch (IllegalArgumentException iae) {
                            // Si hay otro problema (ejemplo: cédula duplicada), muestro este error
                            JOptionPane.showMessageDialog(null, iae.getMessage());
                        }
                        break;
                    }

                    // ----------------- CASE 3: Mostrar ingresos -----------------
                    case 3: {
                        // reviso si no hay viajes creados
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break;}
                        // creo un stringbuilder para armar el texto de ingresos
                        StringBuilder sb = new StringBuilder("Ingresos de los viajes:\n");
                        // recorro todos los viajes

                        for (int i = 0; i < sistema.getViajes().size(); i++) {
                            Viajes vj = sistema.getViajes().get(i);// tomo cada viaje

                            // agrego la informacion al stringbuilder
                            sb.append("Viaje ").append(i)
                                    .append(" - ").append(vj.getOrigen())
                                    .append(" → ").append(vj.getDestino())
                                    .append(" | Fecha: ").append(vj.getFechaSalida())
                                    .append(" | Vehículo: ").append(vj.getVehiculo().getClass().getSimpleName())
                                    .append(" | Conductor: ").append(vj.getConductor().getNombre())
                                    .append(" | Pasajeros: ").append(vj.getClientes().size())
                                    .append(" | Ingresos: $").append(vj.calcularIngresos())
                                    .append("\n");
                        }
                        // creo un area de texto para mostrar el reporte con JScrollPane
                        JTextArea ta = new JTextArea(sb.toString());
                        ta.setEditable(false);// digo que no se puede editar
                        // creo un scroll por si hay muchos datos
                        JScrollPane sp = new JScrollPane(ta);
                        sp.setPreferredSize(new java.awt.Dimension(600, 300));// defino tamano
                        // muestro la ventana con el reporte

                        JOptionPane.showMessageDialog(null, sp, "Ingresos", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }

                    // ----------------- CASE 4: Cancelar reserva -----------------
                    case 4: {
                        // reviso si hay viajes
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }

                        // muestro lista de viajes
                        StringBuilder lv = new StringBuilder("Viajes disponibles:\n");
                        for (int i = 0; i < sistema.getViajes().size(); i++) {
                            Viajes vi = sistema.getViajes().get(i);
                            lv.append(i).append(". ").append(vi.getOrigen()).append(" → ").append(vi.getDestino())
                                    .append(" | Fecha: ").append(vi.getFechaSalida()).append("\n");
                        }
                        // pido al usuario cual viaje quiere
                        String sel = JOptionPane.showInputDialog(lv.toString() + "\nSeleccione el número de viaje para cancelar una reserva:");
                        if (sel == null) break;// si cancela salgo
                        int idx = Integer.parseInt(sel.trim()); // convierto en numero
                        if (idx < 0 || idx >= sistema.getViajes().size()) // valido rango
                            { JOptionPane.showMessageDialog(null, "Número de viaje no válido."); break; }

                        Viajes vCancel = sistema.getViajes().get(idx);// obtengo el viaje elegido

                        // pido la cedula del cliente a cancelar
                        String cedulaCancel = JOptionPane.showInputDialog("Ingrese la cédula del cliente a cancelar:");
                        if (cedulaCancel == null || cedulaCancel.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Cédula inválida."); break; }
                        // busco el cliente con streams
                        Optional<Cliente> encontrado = vCancel.getClientes().stream()
                                .filter(c -> c.getCedula().equals(cedulaCancel.trim()))
                                .findFirst();

                        if (encontrado.isPresent()) {
                            vCancel.eliminarClientePorCedula(cedulaCancel.trim());// elimino el cliente
                            JOptionPane.showMessageDialog(null, "Reserva cancelada correctamente para cédula: " + cedulaCancel.trim());
                        } else {
                            JOptionPane.showMessageDialog(null, "No se encontró reserva con la cédula: " + cedulaCancel.trim());
                        }
                        break;
                    }

                    // ----------------- CASE 5: Estadísticas con Streams -----------------
                    case 5: {
                        // reviso si hay viajes
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }
                        // calculo total de pasajeros usando streams
                        long totalPasajeros = sistema.getViajes().stream()
                                .mapToLong(v -> v.getClientes().size())
                                .sum();
                        // calculo promedio de ingresos
                        double promedioIngresos = sistema.getViajes().stream()
                                .mapToDouble(Viajes::calcularIngresos)
                                .average()
                                .orElse(0);
                        // armo el texto de estadisticas
                        String stats = "📊 Estadísticas:\n" +
                                "Total pasajeros (todos los viajes): " + totalPasajeros + "\n" +
                                "Promedio ingresos por viaje: $" + String.format("%.2f", promedioIngresos);
                        // muestro ventana
                        JOptionPane.showMessageDialog(null, stats);
                        break;
                    }

                    // ----------------- CASE 6: Buscar cliente por cédula -----------------
                    case 6: {// reviso si hay viajes
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }

                        // pido la cedula del cliente a buscar
                        String cedulaBuscar = JOptionPane.showInputDialog("Ingrese la cédula del cliente a buscar:");
                        if (cedulaBuscar == null || cedulaBuscar.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Cédula inválida."); break; }
                        boolean encontrado = false;// bandera para saber si lo encontre


                        // recorro todos los viajes
                        for (int i = 0; i < sistema.getViajes().size() && !encontrado; i++) {
                            Viajes vi = sistema.getViajes().get(i);
                            // recorro los clientes de cada viaje

                            for (Cliente cli : vi.getClientes()) {
                                if (cli.getCedula().equals(cedulaBuscar.trim())) {
                                    // si encuentro muestro info
                                    JOptionPane.showMessageDialog(null,
                                            "Cliente encontrado:\n" +
                                                    "Nombre: " + cli.getNombre() + "\n" +
                                                    "Cédula: " + cli.getCedula() + "\n" +
                                                    "Tel: " + cli.getTelefono() + "\n" +
                                                    "Asiento: " + cli.getAsiento() + "\n" +
                                                    "Viaje (índice): " + i + " -> " + vi.getOrigen() + " → " + vi.getDestino() + "\n" +
                                                    "Fecha: " + vi.getFechaSalida() + "\n" +
                                                    "Vehículo: " + vi.getVehiculo().getClass().getSimpleName() + "\n" +
                                                    "Conductor: " + vi.getConductor().toString()
                                    );
                                    encontrado = true;// marco como encontrado
                                    break;
                                }
                            }
                        }
                        // si no lo encontre muestro mensaje

                        if (!encontrado) JOptionPane.showMessageDialog(null, "No se encontró cliente con cédula: " + cedulaBuscar.trim());
                        break;
                    }

                    // ----------------- CASE 7: Mostrar pasajeros en JTable -----------------
                    case 7: {    // reviso si hay viajes

                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }

                        // muestro lista de viajes

                        StringBuilder lv2 = new StringBuilder("Viajes disponibles:\n");
                        for (int i = 0; i < sistema.getViajes().size(); i++) {
                            Viajes vi = sistema.getViajes().get(i);
                            lv2.append(i).append(". ").append(vi.getOrigen()).append(" → ").append(vi.getDestino())
                                    .append(" | Fecha: ").append(vi.getFechaSalida()).append("\n");
                        }
                        // pido viaje a mostrar

                        String sel2 = JOptionPane.showInputDialog(lv2.toString() + "\nSeleccione el número de viaje para ver la tabla de pasajeros:");
                        if (sel2 == null) break;
                        int idxTabla = Integer.parseInt(sel2.trim());
                        if (idxTabla < 0 || idxTabla >= sistema.getViajes().size()) { JOptionPane.showMessageDialog(null, "Número de viaje no válido."); break; }

                        Viajes viajeTabla = sistema.getViajes().get(idxTabla);
                        List<Cliente> listaClientes = viajeTabla.getClientes();// obtengo lista clientes

                        // defino columnas para la tabla

                        String[] columnas = {"Nombre", "Cédula", "Teléfono", "Asiento", "Origen", "Destino", "Fecha", "Vehículo", "Conductor"};

                        // lleno datos de la tabla

                        Object[][] datos = new Object[listaClientes.size()][columnas.length];

                        for (int i = 0; i < listaClientes.size(); i++) {
                            Cliente cli = listaClientes.get(i);
                            datos[i][0] = cli.getNombre();
                            datos[i][1] = cli.getCedula();
                            datos[i][2] = cli.getTelefono();
                            datos[i][3] = cli.getAsiento();
                            datos[i][4] = viajeTabla.getOrigen();
                            datos[i][5] = viajeTabla.getDestino();
                            datos[i][6] = viajeTabla.getFechaSalida();
                            datos[i][7] = viajeTabla.getVehiculo().getClass().getSimpleName();
                            datos[i][8] = viajeTabla.getConductor().getNombre();
                        }

                        // creo modelo de tabla

                        DefaultTableModel model = new DefaultTableModel(datos, columnas) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false; // tabla solo lectura// no permito editar
                            }
                        };
                        // creo tabla con el modelo
                        JTable table = new JTable(model);
                        // agrego scroll

                        JScrollPane scroll = new JScrollPane(table);

                        // muestro la tabla en ventana
                        scroll.setPreferredSize(new java.awt.Dimension(900, 300));
                        JOptionPane.showMessageDialog(null, scroll, "Pasajeros del viaje " + idxTabla, JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }

                    // ----------------- CASE 8: Salir -----------------
                    case 8: // Salir del programa
                        JOptionPane.showMessageDialog(null, "¡Gracias por usar el sistema!");
                        break;

                    // ----------------- DEFAULT -----------------
                    default:// Si el usuario escribe un número fuera del menú
                        JOptionPane.showMessageDialog(null, "Opción no válida.");
                }

            } catch (NumberFormatException nfe) {
                // Si el usuario escribe letras donde debería ir un número, muestro este mensaje
                JOptionPane.showMessageDialog(null, "Debe ingresar un número válido.");
            } catch (Exception ex) {
                // Si ocurre cualquier otro error inesperado, lo muestro aquí
                JOptionPane.showMessageDialog(null, "Ocurrió un error: " + ex.getMessage());
            }
        } while (opcion != 8);// El menú se repite hasta que el usuario elija la opción 8 (salir)
    }
}
