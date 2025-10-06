package modelo;


// La clase Usuario es abstracta
public abstract class Usuario {


    // Atributos propios de la clase
    private  String nombre, cedula, telefono;

    // Constructor vacío
    public Usuario() {
    }

    // Constructor con parámetros
    public Usuario(String nombre, String cedula, String telefono) {
        this.nombre = nombre;
        this.cedula = cedula;
        this.telefono = telefono;
    }


    // Métodos getters
    public String getNombre() {
        return nombre;
    }

    public String getCedula() {
        return cedula;
    }

    public String getTelefono() {
        return telefono;
    }

    // Método que devuelve la información completa del conductor como texto
    @Override
    public String toString() {
        return "NOMBRE: " + nombre + " | CEDULA: " + cedula + " | TELEFONO: " +  telefono ;
    }
}


