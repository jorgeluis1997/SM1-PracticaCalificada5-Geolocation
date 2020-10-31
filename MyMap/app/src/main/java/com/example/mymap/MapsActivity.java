package com.example.mymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "Estilo del Mapa";
    public static final int REQUEST_LOCATION_PERMISSION = 1;
    public GoogleMap mMap;

    public LatLng casa;
    public float zoom;

    public static final String MAP_STYLE_NORMAL = "NORMAL";
    public static final String MAP_STYLE_HIBRIDO = "HÍBRIDO";
    public static final String MAP_STYLE_SATELITAL = "SATELITAL";
    public static final String MAP_STYLE_TERRENO = "TERRENO";

    double longitudeBest, latitudeBest;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        //Set fragment
        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, mapFragment).commit();
        mapFragment.getMapAsync(this);
        casa = new LatLng(-18.007741, -70.244397);
        zoom = 15;


        // spiner
        List<String> lstStyle = new ArrayList<>();
        lstStyle.add(MAP_STYLE_NORMAL);
        lstStyle.add(MAP_STYLE_HIBRIDO);
        lstStyle.add(MAP_STYLE_SATELITAL);
        lstStyle.add(MAP_STYLE_TERRENO);

        ArrayAdapter<String> adapterSpinnerMapStyle = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, lstStyle);
        final Spinner spinerMapStyle = findViewById(R.id.spinner_map_theme);
        spinerMapStyle.setAdapter(adapterSpinnerMapStyle);
        spinerMapStyle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setChangeMapStyle(spinerMapStyle.getSelectedItem().toString());
                Log.d("MAPP>", spinerMapStyle.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Button btnCalculateDistance = findViewById(R.id.btn_calculate_distance);
        btnCalculateDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String provider = "Japon";
                if (mMap.getMapType() == GoogleMap.MAP_TYPE_HYBRID) {
                    provider = "Italia";
                } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    provider = "Japon";
                } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
                    provider = "Alemania";
                } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN) {
                    provider = "Francia";
                }
                float distance = CalcularDistancia(provider);
                String messagedistance = String.valueOf("Distancia a " + provider + " " + distance + " KM");
                showDistancaInDialog(messagedistance);
            }
        });
    }

    private void setInfoWindowClickToPanorama(GoogleMap map) {
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() == "poi") {
                    StreetViewPanoramaOptions options = new StreetViewPanoramaOptions().position(marker.getPosition());
                    SupportStreetViewPanoramaFragment supportStreetViewPanoramaFragment = SupportStreetViewPanoramaFragment.newInstance(options);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, supportStreetViewPanoramaFragment).addToBackStack(null).commit();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.normal_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Map ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(casa, zoom));
        GroundOverlayOptions casaOverlay = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.ic_location)).position(casa, 100);
        mMap.addGroundOverlay(casaOverlay);
        setMapClick(mMap);
        setPoiClick(mMap);
        setInfoWindowClickToPanorama(mMap);
        enableMyLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }

    //Enable location
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void setMapClick(final GoogleMap map) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(), "Lat: %1$.5f, Long: %2$.5f", latLng.latitude, latLng.longitude);
                map.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.app_name)).snippet(snippet).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            }
        });
    }

    private void setPoiClick(final GoogleMap map) {
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                Marker marker = mMap.addMarker(new MarkerOptions().position(pointOfInterest.latLng).title(pointOfInterest.name));
                marker.showInfoWindow();
                marker.setTag("poi");
            }
        });
    }


    private float CalcularDistancia(String provider) {
        float distance = 0;
        double latitud = 0;
        double longitud = 0;
        Location crntLocation = new Location("crntlocation");
        crntLocation.setLatitude(casa.latitude);
        crntLocation.setLongitude(casa.longitude);
        switch (provider) {
            case "Japon":
                latitud = 35.680513;
                longitud = 139.769051;
                break;
            case "Alemania":
                latitud = 52.516934;
                longitud = 13.403190;
                break;
            case "Italia":
                latitud = 41.902609;
                longitud = 12.494847;
                break;
            case "Francia":
                latitud = 48.843489;
                longitud = 2.355331;
                break;
        }
        Location location = new Location(provider);
        location.setLatitude(latitud);
        location.setLongitude(longitud);
        distance = crntLocation.distanceTo(location) / 1000; //in km
        return distance;
    }

    private void showDistancaInDialog(String resultMessage) {
        new AlertDialog.Builder(MapsActivity.this)
                .setTitle("Distancia")
                .setMessage(resultMessage)
                .show();
    }

    private void setChangeMapStyle(String typeMapStyle) {
        switch (typeMapStyle) {
            case MAP_STYLE_HIBRIDO:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return;
            case MAP_STYLE_SATELITAL:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return;
            case MAP_STYLE_TERRENO:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return;
            case MAP_STYLE_NORMAL:
            default:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return;
        }
    }

    private final LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            longitudeBest = location.getLongitude();
            latitudeBest = location.getLatitude();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MapsActivity.this, "Best Provider update" + longitudeBest + " " + latitudeBest, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

}
