package com.casaucao.asyncviews;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Esta aplicación Android intenta enseñar una posible forma de cargar vistas de forma dinámica.
 *
 * @author Eric Z. Casaucao
 * @email ematic.android@gmail.com
 * @twitter @casaucao
 */
public class MainActivity extends Activity {
	private static final int MAX_VIEWS = 10000; // Número de views que inflaremos
	private LinearLayout ll_container; // Contenedor de las vistas que inflaremos
	private LinearLayout ll; // Contenedor temporal (será hijo de ll_container)
	private ProgressDialog pd; // Diálogo de progreso para informar que se están realizando tareas en segundo plano

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ll_container = (LinearLayout) findViewById(R.id.activity_main_ll_container);
		// Configuramos el diálogo de progreso
		setupProgressDialog();

		// Asignamos los listeners a los botones
		findViewById(R.id.activity_main_bt_inflar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				inflarAsync();
			}
		});

		findViewById(R.id.activity_main_bt_desinflar).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				desinflar();
			}
		});
	}

	/**
	 * Método para configurar el diálog de progreso
	 */
	private void setupProgressDialog() {
		if (pd != null) return; // Nos aseguramos de que no se haya configurado antes

		pd = new ProgressDialog(MainActivity.this);
		pd.setMessage(getResources().getString(R.string.pd_msg));
		pd.setTitle(getResources().getString(R.string.pd_title));
		pd.setCancelable(false);
		pd.setIndeterminate(true);
	}

	/**
	 * Método para inflar asíncronamente las vistas
	 */
	private void inflarAsync() {
		// Desinflamos
		desinflar();
		// Iniciamos la tarea asíncrona que se encargará de inflar las vistas
		ThreadRender thread = new ThreadRender();
		thread.start();
	}

	/**
	 * Método para desinflar las vistas
	 */
	private void desinflar() {
		if (ll_container != null) {
			ll_container.removeAllViews(); // Quitamos todas las vistas que hubiera
		}
	}

	/**
	 * Clase encargada de inflar dinámicamente las vistas
	 */
	private class ThreadRender extends Thread {
		@Override
		public void run() {
			Looper.prepare();

			mHandler.sendEmptyMessage(1); // Mostramos el diálogo
			inflarSync(); // Entramos en materia :)
		}
	}

	/**
	 * Handler usado para realizar tareas en modo ui-safe.
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					mostrarDialogo(true);
					break; // Mostramos diálogo
				case 2:
					ll_container.addView(ll);
					mostrarDialogo(false);
					break; // Ya hemos terminado. Quitamos el diálogo
			}
		}
	};

	/**
	 * Método para mostrar u ocultar el diálogo de progreso
	 *
	 * @param show boolean indicando si debe mostrarse o no
	 */
	private void mostrarDialogo(boolean show) {
		if (pd != null) {
			if (show) {
				pd.show(); // Mostramos el diálogo
			} else {
				pd.dismiss(); // Lo ocultmaos
			}
		}
	}

	/**
	 * Método para inflar de forma SÍNCRONA las vistas
	 */
	private void inflarSync() {
		ll = new LinearLayout(getApplicationContext()); // Creamos nuestro contenedor temporal
		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) ll_container.getLayoutParams(); // Y copiamos los mismos LayoutParams del contenedor principal
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setLayoutParams(lp); // Se los asignamos

		LayoutInflater inflate = LayoutInflater.from(getApplicationContext());

		for (int i = 0; i < MAX_VIEWS; i++) {
			final LinearLayout item = (LinearLayout) inflate.inflate(R.layout.item_view, ll, false);

			// Pintamos algunos colores
			for (int j = 0; j < item.getChildCount(); j++) {
				final LinearLayout childItem = (LinearLayout) item.getChildAt(j);
				childItem.setBackgroundColor(getRandomColor()); // Asignamos un color aleatorio a cada celda
			}

			((TextView) item.findViewById(R.id.item_view_tv)).setText("" + i); // Asignamos el número de item

			ll.addView(item);
		}

		mHandler.sendEmptyMessage(2); // Informamos que ya hemos terminado
	}

	/**
	 * Método para generar un color aleatorio
	 *
	 * @return int con el color generado
	 */
	private int getRandomColor() {
		int r = (int) (Math.random() * 255);
		int g = (int) (Math.random() * 255);
		int b = (int) (Math.random() * 255);

		return Color.rgb(r, g, b);
	}
}
