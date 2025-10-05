package modelo;


// La clase Cliente hereda de la clase Usuario
public class Cliente extends Usuario {

    // Atributo propio de la clase
     private String correo;
     private int asientos;



    // Constructor vacío
    public Cliente() {
    }

    // Constructor con parámetros
    public Cliente(String nombre, String cedula, String telefono, String correo, int asientos) {
        super(nombre, cedula, telefono);
        this.correo = correo;
        this.asientos = asientos;
    }

    // Método getter
    public String getCorreo() {
        return correo;
    }

    public int getAsiento() {
        return asientos;
    }

}

