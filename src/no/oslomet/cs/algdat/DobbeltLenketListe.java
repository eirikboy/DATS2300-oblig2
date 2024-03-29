package no.oslomet.cs.algdat;


////////////////// class DobbeltLenketListe //////////////////////////////


import sun.reflect.generics.reflectiveObjects.GenericArrayTypeImpl;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.swing.*;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;


public class DobbeltLenketListe<T> implements Liste<T> {

    /**
     * Node class
     *
     * @param <T>
     */
    private static final class Node<T> {
        private T verdi;                   // nodens verdi
        private Node<T> forrige, neste;    // pekere

        private Node(T verdi, Node<T> forrige, Node<T> neste) {
            this.verdi = verdi;
            this.forrige = forrige;
            this.neste = neste;
        }

        private Node(T verdi) {
            this(verdi, null, null);
        }
    }

    private Node<T> finnNode(int indeks) {
        Node<T> node;
        if (indeks < antall / 2) {
            node = hode;
            for (int i = 0; i < indeks; i++) {
                node = node.neste;
            }
        } else {
            node = hale;
            for (int i = indeks; i < antall - 1; i++) {
                node = node.forrige;
            }
        }
        return node;
    }

    // instansvariabler
    private Node<T> hode;          // peker til den første i listen
    private Node<T> hale;          // peker til den siste i listen
    private int antall;            // antall noder i listen
    private int endringer;         // antall endringer i listen

    public DobbeltLenketListe() {
        hode = null;
        hale = null;
        antall = 0;
        endringer = 0;
    }

    public DobbeltLenketListe(T[] a) {
        if (a == null) {//alternativt Objects.requireNonNull(a, "Tabellen a er null!");
            throw new NullPointerException("Tabellen a er null!");
        } else {

            for (int i = 0; i < a.length; i++) {
                if (hode == null) {
                    if (a[i] != null) {
                        hode = new Node<>(a[i], null, hale);
                        hale = hode;
                        antall++;
                    }
                } else {
                    if (a[i] != null) {
                        hale.neste = new Node<>(a[i], hale, null);
                        hale = hale.neste;
                        antall++;
                    }
                }
            }
        }
    }

    public Liste<T> subliste(int fra, int til) {
        fratilKontroll(antall(), fra, til);
        T[] sub = (T[]) new Object[til - fra];
        int j = 0;
        endringer = 0;
        Node<T> node = finnNode(fra);

        for (int i = fra; i < til; i++) {
            sub[j] = node.verdi;
            node = node.neste;
            j++;
        }
        DobbeltLenketListe<T> nyListe = new DobbeltLenketListe<>(sub);
        return nyListe;

    }

    private void fratilKontroll(int antall, int fra, int til) {
        if (fra < 0) {
            throw new IndexOutOfBoundsException("Fra (" + fra + ") er negativt!");
        }

        if (til > antall) {
            throw new IndexOutOfBoundsException("Til (" + til + ") er størren enn antall (" + antall + ")!");
        }

        if (fra > til) {
            throw new IllegalArgumentException("Fra (" + fra + ") er større enn til (" + til + ")!");
        }
    }

    @Override
    public int antall() {
        return antall;
    }

    @Override
    public boolean tom() {
        if (antall == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean leggInn(T verdi) {
        Objects.requireNonNull(verdi, "Null-verdier er ikke tillatt!");
        if (antall == 0) {
            hode = new Node<>(verdi, null, hale);
            hale = hode;
        } else {
            hale.neste = new Node<>(verdi, hale, null);
            hale = hale.neste;
        }
        antall++;
        endringer++;
        return true;
    }

    @Override
    public void leggInn(int indeks, T verdi) {
        Objects.requireNonNull(verdi, "Null-verdier er ikke tillatt!");

        if (!(0 <= indeks && indeks <= antall)){
            throw new IndexOutOfBoundsException("Feil indeks");
        }

        Node<T> node = new Node<>(verdi);
        if (tom()){//Tom liste
            leggInn(verdi);
            return;
        } else if (indeks == 0){//Legges på første plass
            node.neste = hode;
            node.forrige = null;
            if (hode != null){
                hode.forrige = node;
            }
            hode = node;
        } else if (indeks == antall){//Legges bakerst
            leggInn(verdi);
            return;
        } else {//Legges i mellom
            Node<T> finn = hode;
            for (int i = 0; i < indeks-1; i++){
                finn = finn.neste;
            }
            node.neste = finn.neste;
            node.forrige = finn;
            finn.neste = node;
            node.neste.forrige = node;
        }
        antall++;
        endringer++;
    }


    @Override
    public boolean inneholder(T verdi) {
        if (indeksTil(verdi) == -1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public T hent(int indeks) {
        indeksKontroll(indeks, false);
        return finnNode(indeks).verdi;
    }

    @Override
    public int indeksTil(T verdi) {
        Node<T> node = hode;
        for (int i = 0; i < antall; i++) {
            if (node.verdi.equals(verdi)) {
                return i;
            } else {
                node = node.neste;
            }
        }
        return -1;
    }

    @Override
    public T oppdater(int indeks, T nyverdi) {
        Objects.requireNonNull(nyverdi, "Null-verdier er ikke tillatt!");

        indeksKontroll(indeks, false);

        Node<T> hentNode = finnNode(indeks);
        T returVerdi = hentNode.verdi;
        hentNode.verdi = nyverdi;
        endringer++;
        return returVerdi;
    }

    @Override
    public boolean fjern(T verdi) {
        if(verdi == null || antall == 0)
            return false;
        Node<T> current = hode;
        if(hode.verdi.equals(verdi)) {
            if(antall == 1) {
                hode = hale = null;
            }
            else {
                hode.neste.forrige = null;
                hode = hode.neste;
            }
            antall--;
            endringer++;
            return true;
        }
        for(int scan=0; scan<antall; scan++) {
            if(current.verdi.equals(verdi)) {
                if(scan == antall-1) {
                    hale.forrige.neste = null;
                    hale = hale.forrige;
                }
                else {
                    current.forrige.neste = current.neste;
                    current.neste.forrige = current.forrige;
                }
                antall--;
                endringer++;
                return true;
            }
            else {
                current = current.neste;
            }
        }
        return false;
    }

    @Override
    public T fjern(int indeks) {
        if(antall == 0 || indeks < 0 || antall <= indeks) {
            throw new IndexOutOfBoundsException();
        }
        if(antall == 1) {
            antall--;
            endringer++;
            T verdi = hode.verdi;
            hode = hale = null;
            return verdi;
        }
        T verdi;
        antall--;
        endringer++;
        if(indeks == 0){
            verdi = hode.verdi;
            hode.neste.forrige = null;
            hode = hode.neste;
        }
        else if(indeks == antall) {
            verdi = hale.verdi;
            hale.forrige.neste = null;
            hale = hale.forrige;
        }
        else {
            Node<T> node = hode;
            for(int scan=0; scan<indeks; scan++) {
                node = node.neste;
            }
            verdi = node.verdi;
            node.forrige.neste = node.neste;
            node.neste.forrige = node.forrige;
        }
        if(antall == 0) {
            hode = hale = null;
        }
        return verdi;
    }


    @Override
    public void nullstill() {
        Node<T> node = hode;

        while (node != hale){
            Node<T> neste = node.neste;
            node.neste = node.forrige = null;
            node.verdi = null;
            node = neste;
        }

        hode = hale = null;
        antall = 0;
        endringer = 0;
    }

    public void nullstillVersjon2() {
        for (int i = 0; i < antall; i++){
            fjern(0);
        }
    }

    @Override
    public String toString() {
        if (tom()){
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Node<T> node = hode;
        for (int i = 0; i < antall(); i++) {
            if (node != null){
                sb.append(node.verdi);
                node = node.neste;
            }
            if (node != null) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String omvendtString() {
        if (tom()){
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Node<T> node = hale;
        for (int i = 0; i < antall(); i++) {
            if (node != null){
                sb.append(node.verdi);
                node = node.forrige;
            }
            if (node != null) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new DobbeltLenketListeIterator();
    }

    public Iterator<T> iterator(int indeks) {
        indeksKontroll(indeks, false);
        return new DobbeltLenketListeIterator(indeks);

    }

    private class DobbeltLenketListeIterator implements Iterator<T> {
        private Node<T> denne;
        private boolean fjernOK;
        private int iteratorendringer;


        private DobbeltLenketListeIterator(int indeks) {
            denne = finnNode(indeks);

            fjernOK = false;
            iteratorendringer = endringer;

        }


        private DobbeltLenketListeIterator(){
            denne = hode;     // p starter på den første i listen
            fjernOK = false;  // blir sann når next() kalles
            iteratorendringer = endringer;  // teller endringer
        }

        @Override
        public boolean hasNext() {
            return denne != null;
        }

        @Override
        public T next() {
            if (iteratorendringer != endringer) {
                throw new ConcurrentModificationException();
            }
            if (hasNext() != true) {
                throw new NoSuchElementException();
            }
            fjernOK = true;
            T value = denne.verdi;
            denne = denne.neste;

            return value;
        }

        @Override
        public void remove(){
            if(!fjernOK) throw new IllegalStateException();
            if(iteratorendringer != endringer ) throw new ConcurrentModificationException();
            if(antall == 0) {
                return;
            }
            if(antall == 1) {
                hode = null;
                hale = null;
            }
            else if(denne == null) {
                hale = hale.forrige;
                hale.neste = null;
            }
            else if(denne.forrige == hode) {
                hode = denne;
                denne.forrige = null;
            }
            else {
                denne.forrige.forrige.neste = denne;
                denne.forrige = denne.forrige.forrige;
            }
            endringer++;
            iteratorendringer++;
            antall--;
            fjernOK = false;
        }

    } // class DobbeltLenketListeIterator

//        @Override
//        public void remove() {
//            if(!fjernOK){
//                throw new IllegalStateException();
//            }
//            if(iteratorendringer != endringer){
//                throw new ConcurrentModificationException();
//            }
//            fjernOK = false;
//            Node<T> slett = hode;
//            if(antall == 1){
//                hode = null;
//                hale = null;
//            }
//
//            else if(denne == null){
//                slett = hale;
//                hale = hale.forrige;
//                hale.neste = null;
//            }
//
//            else if(denne.forrige == null){
//                slett = hode;
//                hode = hode.neste;
//                hode.forrige = null;
//            }
//
//            else{
////                slett = denne.forrige;
////                slett.forrige.neste = slett.neste;
////                slett.neste.forrige = slett.forrige;
//                denne.forrige = denne.forrige.forrige;
//                denne.forrige.forrige.neste = denne;
//            }
////            slett.verdi = null;
////            slett.forrige = null;
////            slett.neste = null;
//
//            endringer++;
//            iteratorendringer++;
//            antall --;
//
//        }
//
//
//
//    } // class DobbeltLenketListeIterator

//    public static <T> void sorter(Liste<T> liste, Comparator<? super T> c) {
//        for (int i = 0; i < liste.antall(); i++){
//            int sjekk = c.compare(liste.hent(i),  liste.hent(i+1));
//            if (sjekk > 0){
//                for (int j = i; j < liste.antall(); j++){
//                    //liste.oppdater();
//                }
//            }
//        }
//    }

    public static <T> void sorter(Liste<T> liste, Comparator<? super T> c) {
        for(int i=0; i<liste.antall(); i++) {
            for(int j=0; j<liste.antall()-i-1; j++) {
                if(c.compare(liste.hent(j), liste.hent(j+1)) > 0) {
                    T value1 = liste.hent(j);
                    T value2 = liste.hent(j+1);
                    liste.oppdater(j, value2);
                    liste.oppdater(j+1, value1);
                }
            }
        }
    }

} // class DobbeltLenketListe


