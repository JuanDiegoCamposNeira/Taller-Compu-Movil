package com.example.taller2;

class User {
    // Attributes
    public String uid = "";
    public String Nombres = "";
    public String Apellidos = "";
    public String Email = "";
    public String Password = "";
    public String Id = "";
    public String Disponible = "";
    public String Latitud = "";
    public String Longitud = "";

    // Constructor(s)
    public User() {

    }

    public User(String uid, String Nombres, String Disponible, String Latitud, String Longitud, String Apellidos, String Email, String Password, String Id) {
        this.uid = uid;
        this.Nombres = Nombres;
        this.Disponible = Disponible;
        this.Latitud = Latitud;
        this.Longitud = Longitud;
        this.Apellidos = Apellidos;
        this.Email = Email;
        this.Password = Password;
        this.Id = Id;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid='" + uid + '\'' +
                ", Nombres='" + Nombres + '\'' +
                ", Apellidos='" + Apellidos + '\'' +
                ", Email='" + Email + '\'' +
                ", Password='" + Password + '\'' +
                ", Id='" + Id + '\'' +
                ", Disponible='" + Disponible + '\'' +
                ", Latitud='" + Latitud + '\'' +
                ", Longitud='" + Longitud + '\'' +
                '}';
    }
}