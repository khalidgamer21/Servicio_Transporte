package main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import modelo.*;
import patron_diseno.*;
import excepciones.AsientosInsuficientes;

public class App {
    public static void main(String[] args) {
        SistemaTransporte sistema = SistemaTransporte.getInstancia();
        int opcion = 0;

        // Formateadores reutilizables
        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter horaFormatter  = DateTimeFormatter.ofPattern("HH:mm");

        do {
            try {
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
                String input = JOptionPane.showInputDialog(menu);

                // Si el usuario presiona Cancel, input será null -> salimos
                if (input == null) break;

                opcion = Integer.parseInt(input.trim());

                switch (opcion) {
                    // ----------------- CASE 1: Crear viaje -----------------
                    case 1: {
                        String tipoStr = JOptionPane.showInputDialog(
                                "*** Crear Viaje ***\n" +
                                        "Seleccione el tipo de vehículo:\n" +
                                        "1. Bus (40 puestos, $50.000)\n" +
                                        "2. Minivan (10 puestos, $20.000)"
                        );
                        if (tipoStr == null) break;
                        int tipoVehiculo = Integer.parseInt(tipoStr.trim());
                        if (tipoVehiculo != 1 && tipoVehiculo != 2) {
                            JOptionPane.showMessageDialog(null, "Tipo de vehículo no válido.");
                            break;
                        }

                        Vehiculo vehiculo = VehiculoFactory.crearVehiculo(tipoVehiculo);

                        String origen = JOptionPane.showInputDialog("Ingrese ciudad de origen:");
                        if (origen == null || origen.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Origen inválido."); break; }

                        String destino = JOptionPane.showInputDialog("Ingrese ciudad de destino:");
                        if (destino == null || destino.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Destino inválido."); break; }

                        if (origen.trim().equalsIgnoreCase(destino.trim())) {
                            JOptionPane.showMessageDialog(null, "Origen y destino no pueden ser iguales.");
                            break;
                        }

                        String fechaStr = JOptionPane.showInputDialog("Ingrese la fecha de salida (yyyy-MM-dd)\nEjemplo: 2025-10-21");
                        if (fechaStr == null || fechaStr.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Fecha inválida."); break; }

                        String horaStr = JOptionPane.showInputDialog("Ingrese la hora de salida (HH:mm)\nEjemplo: 14:30");
                        if (horaStr == null || horaStr.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Hora inválida."); break; }

                        LocalDate fecha;
                        LocalTime hora;
                        try {
                            fecha = LocalDate.parse(fechaStr.trim(), fechaFormatter);
                            hora = LocalTime.parse(horaStr.trim(), horaFormatter);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Formato de fecha u hora inválido. Use yyyy-MM-dd y HH:mm.");
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

                        Conductor conductor = new Conductor(
                                nombreConductor.trim(),
                                cedulaConductor.trim(),
                                telefonoConductor.trim(),
                                true,
                                licenciaConductor.trim()
                        );

                        Viajes nuevoViaje = new Viajes(vehiculo, origen.trim(), destino.trim(), fechaSalida, conductor);
                        sistema.agregarViaje(nuevoViaje);

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
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados aún."); break; }

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

                        String sel = JOptionPane.showInputDialog(listaViajes.toString() + "\nSeleccione el número de viaje:");
                        if (sel == null) break;
                        int index = Integer.parseInt(sel.trim());
                        if (index < 0 || index >= sistema.getViajes().size()) { JOptionPane.showMessageDialog(null, "Número de viaje no válido."); break; }

                        Viajes viaje = sistema.getViajes().get(index);

                        String nombreCliente = JOptionPane.showInputDialog("Ingrese el nombre del cliente:");
                        if (nombreCliente == null || nombreCliente.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Nombre inválido."); break; }

                        String cedulaCliente = JOptionPane.showInputDialog("Ingrese la cédula del cliente:");
                        if (cedulaCliente == null || cedulaCliente.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Cédula inválida."); break; }

                        String telefonoCliente = JOptionPane.showInputDialog("Ingrese el teléfono del cliente:");
                        if (telefonoCliente == null || telefonoCliente.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Teléfono inválido."); break; }

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
                                    asientoValido = true; // asiento correcto -> salir del bucle
                                } else {
                                    JOptionPane.showMessageDialog(null, "Número de asiento fuera de rango. Intente de nuevo.");
                                }
                            } catch (NumberFormatException e) {
                                JOptionPane.showMessageDialog(null, "Debe ingresar un número válido.");
                            }
                        }




                        String correoCliente = JOptionPane.showInputDialog("Ingrese el correo del cliente:");
                        if (correoCliente == null || correoCliente.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Correo inválido."); break; }

                        Cliente cliente = new Cliente(
                                nombreCliente.trim(),
                                cedulaCliente.trim(),
                                telefonoCliente.trim(),
                                correoCliente.trim(),
                                asiento
                        );
                        try {
                            viaje.agregarCliente(cliente);

                            JOptionPane.showMessageDialog(null,
                                    "Tiquete generado:\n" +
                                            "Cliente: " + cliente.toString() + "\n" +
                                            "Viaje: " + viaje.getOrigen() + " → " + viaje.getDestino() + "\n" +
                                            "Fecha: " + viaje.getFechaSalida() + "\n" +
                                            "Vehículo: " + viaje.getVehiculo().getClass().getSimpleName() + "\n" +
                                            "Conductor: " + viaje.getConductor().toString() + "\n" +
                                            "Precio: $" + viaje.getVehiculo().calcularTarifa()
                            );
                        } catch (AsientosInsuficientes aie) {
                            JOptionPane.showMessageDialog(null, aie.getMessage());
                        } catch (IllegalArgumentException iae) {
                            JOptionPane.showMessageDialog(null, iae.getMessage());
                        }
                        break;
                    }

                    // ----------------- CASE 3: Mostrar ingresos -----------------
                    case 3: {
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }
                        StringBuilder sb = new StringBuilder("Ingresos de los viajes:\n");
                        for (int i = 0; i < sistema.getViajes().size(); i++) {
                            Viajes vj = sistema.getViajes().get(i);
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
                        // Si es muy largo, mostrar en JTextArea dentro de JScrollPane
                        JTextArea ta = new JTextArea(sb.toString());
                        ta.setEditable(false);
                        JScrollPane sp = new JScrollPane(ta);
                        sp.setPreferredSize(new java.awt.Dimension(600, 300));
                        JOptionPane.showMessageDialog(null, sp, "Ingresos", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }

                    // ----------------- CASE 4: Cancelar reserva -----------------
                    case 4: {
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }
                        StringBuilder lv = new StringBuilder("Viajes disponibles:\n");
                        for (int i = 0; i < sistema.getViajes().size(); i++) {
                            Viajes vi = sistema.getViajes().get(i);
                            lv.append(i).append(". ").append(vi.getOrigen()).append(" → ").append(vi.getDestino())
                                    .append(" | Fecha: ").append(vi.getFechaSalida()).append("\n");
                        }
                        String sel = JOptionPane.showInputDialog(lv.toString() + "\nSeleccione el número de viaje para cancelar una reserva:");
                        if (sel == null) break;
                        int idx = Integer.parseInt(sel.trim());
                        if (idx < 0 || idx >= sistema.getViajes().size()) { JOptionPane.showMessageDialog(null, "Número de viaje no válido."); break; }

                        Viajes vCancel = sistema.getViajes().get(idx);
                        String cedulaCancel = JOptionPane.showInputDialog("Ingrese la cédula del cliente a cancelar:");
                        if (cedulaCancel == null || cedulaCancel.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Cédula inválida."); break; }

                        Optional<Cliente> encontrado = vCancel.getClientes().stream()
                                .filter(c -> c.getCedula().equals(cedulaCancel.trim()))
                                .findFirst();

                        if (encontrado.isPresent()) {
                            vCancel.eliminarClientePorCedula(cedulaCancel.trim());
                            JOptionPane.showMessageDialog(null, "Reserva cancelada correctamente para cédula: " + cedulaCancel.trim());
                        } else {
                            JOptionPane.showMessageDialog(null, "No se encontró reserva con la cédula: " + cedulaCancel.trim());
                        }
                        break;
                    }

                    // ----------------- CASE 5: Estadísticas con Streams -----------------
                    case 5: {
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }

                        long totalPasajeros = sistema.getViajes().stream()
                                .mapToLong(v -> v.getClientes().size())
                                .sum();

                        double promedioIngresos = sistema.getViajes().stream()
                                .mapToDouble(Viajes::calcularIngresos)
                                .average()
                                .orElse(0);

                        String stats = "📊 Estadísticas:\n" +
                                "Total pasajeros (todos los viajes): " + totalPasajeros + "\n" +
                                "Promedio ingresos por viaje: $" + String.format("%.2f", promedioIngresos);

                        JOptionPane.showMessageDialog(null, stats);
                        break;
                    }

                    // ----------------- CASE 6: Buscar cliente por cédula -----------------
                    case 6: {
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }

                        String cedulaBuscar = JOptionPane.showInputDialog("Ingrese la cédula del cliente a buscar:");
                        if (cedulaBuscar == null || cedulaBuscar.trim().isEmpty()) { JOptionPane.showMessageDialog(null, "Cédula inválida."); break; }
                        boolean encontrado = false;

                        for (int i = 0; i < sistema.getViajes().size() && !encontrado; i++) {
                            Viajes vi = sistema.getViajes().get(i);
                            for (Cliente cli : vi.getClientes()) {
                                if (cli.getCedula().equals(cedulaBuscar.trim())) {
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
                                    encontrado = true;
                                    break;
                                }
                            }
                        }
                        if (!encontrado) JOptionPane.showMessageDialog(null, "No se encontró cliente con cédula: " + cedulaBuscar.trim());
                        break;
                    }

                    // ----------------- CASE 7: Mostrar pasajeros en JTable -----------------
                    case 7: {
                        if (sistema.getViajes().isEmpty()) { JOptionPane.showMessageDialog(null, "No hay viajes creados."); break; }

                        StringBuilder lv2 = new StringBuilder("Viajes disponibles:\n");
                        for (int i = 0; i < sistema.getViajes().size(); i++) {
                            Viajes vi = sistema.getViajes().get(i);
                            lv2.append(i).append(". ").append(vi.getOrigen()).append(" → ").append(vi.getDestino())
                                    .append(" | Fecha: ").append(vi.getFechaSalida()).append("\n");
                        }
                        String sel2 = JOptionPane.showInputDialog(lv2.toString() + "\nSeleccione el número de viaje para ver la tabla de pasajeros:");
                        if (sel2 == null) break;
                        int idxTabla = Integer.parseInt(sel2.trim());
                        if (idxTabla < 0 || idxTabla >= sistema.getViajes().size()) { JOptionPane.showMessageDialog(null, "Número de viaje no válido."); break; }

                        Viajes viajeTabla = sistema.getViajes().get(idxTabla);
                        List<Cliente> listaClientes = viajeTabla.getClientes();

                        String[] columnas = {"Nombre", "Cédula", "Teléfono", "Asiento", "Origen", "Destino", "Fecha", "Vehículo", "Conductor"};
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

                        DefaultTableModel model = new DefaultTableModel(datos, columnas) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false; // tabla solo lectura
                            }
                        };
                        JTable table = new JTable(model);
                        JScrollPane scroll = new JScrollPane(table);
                        scroll.setPreferredSize(new java.awt.Dimension(900, 300));
                        JOptionPane.showMessageDialog(null, scroll, "Pasajeros del viaje " + idxTabla, JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }

                    // ----------------- CASE 8: Salir -----------------
                    case 8:
                        JOptionPane.showMessageDialog(null, "¡Gracias por usar el sistema!");
                        break;

                    // ----------------- DEFAULT -----------------
                    default:
                        JOptionPane.showMessageDialog(null, "Opción no válida.");
                }

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(null, "Debe ingresar un número válido.");
            } catch (Exception ex) {
                // Captura general para errores inesperados
                JOptionPane.showMessageDialog(null, "Ocurrió un error: " + ex.getMessage());
            }
        } while (opcion != 8);
    }
}
