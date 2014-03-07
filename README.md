Fragment Argument Processor
===========================

> Life is too short to write Fragment factories


Android fragments are supposed to be re-instanciated at will by the system, it means that :

 - They cannot have arguments in their constructors.
 - They cannot retain fields set by their creators.

The most popular (and recommended) pattern for fragments with arguments is the ["newInstance factory"](https://plus.google.com/+AndroidDevelopers/posts/bCD7Zvd945d).

Here is an example of a fragment that uses this pattern :

```java
	public class ExampleFragment extends Fragment {
	
		private static final String ARG_TEXT = "text";
		private static final String ARG_IMAGE_RES = "imageRes";
	
		public static ExampleFragment newInstance(String text, int imageRes) {
			ExampleFragment fragment = new ExampleFragment();
			Bundle args = new Bundle();
			args.putString(ARG_TEXT, text);
			args.putInt(ARG_IMAGE_RES, imageRes);
			fragment.setArguments(args);
			return fragment;
		}
	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.fragment_layout, container, false);
	
			Bundle args = getArguments();
	
			TextView textView = (TextView) root.findViewById(R.id.textView);
			textView.setText(args.getString(ARG_TEXT));

			ImageView imageView = (ImageView) root.findViewById(R.id.imageView);
			imageView.setImageResource(args.getInt(ARG_IMAGE_RES));
	
			return root;
		}
	}
```

You then instanciate the fragment with :

```java
	ExampleFragment fragment = ExampleFragment.newInstance("Hello world", R.drawable.smile);
```

FAP (Fragment Argument Processor) is a Java Annotation processor that enables you to write only the logic of your fragment. It handles for you the newInstance factory pattern and the arguments handling.

To do this, FAP creates a new class named `[yourFragment]Arguments`. You can use it to create your fragment with the correct arguments and within your fragment to acces your arguments.

Here is the previous fragment written with FAP :

```java
	@FragmentArguments({
		@FragmentArgument(type = String.class, name = "text"),
		@FragmentArgument(type = int.class, name = "imageRes")
	})
	public class ExampleFragment extends Fragment {
	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View root = inflater.inflate(R.layout.fragment_layout, container, false);

			ExampleFragmentArguments args = new ExampleFragmentArguments(this);
	
			TextView textView = (TextView) root.findViewById(R.id.textView);
			textView.setText(args.text());

			ImageView imageView = (ImageView) root.findViewById(R.id.imageView);
			imageView.setImageResource(args.imageRes());
	
			return root;
		}
	}
```

You then instanciate the fragment with :

```java
	ExampleFragment fragment = ExampleFragmentArguments.newExampleFragment("Hello world", R.drawable.smile);
```

As you can see, you don't have to write the factory anymore: it is handled for you.


Limitations
-----------

* Argument names must be valid java identifiers: `[a-zA-Z_][a-zA-Z0-9_]*`
* Argument types must be valid for a bundle or they will be ignored:
	- Primitive (eg. int.class)
	- Primitive array (eg. int[].class)
	- String, CharSequence or one of their subclasses
	- Bundle, SparseArray, IBinder or one of their subclasses
	- Parcelable, Serializable or one of their subclasses
	- String[], CharSequence[]
	- Parcelable[]


Usage in Eclipse
----------------

1. Download [FragmentArgumentProcessor.jar](https://github.com/SalomonBrys/Android-FragmentArgumentProcessor/raw/master/FragmentArgumentProcessor.jar)
2. Put it in your Android project `libs` directory
3. In your eclipse project properties > Java Compiler > Annotation Processing : Check "Enable project specific settings" and "Enable annotation processing"
4. In your eclipse project properties > Java Compiler > Annotation Processing > Factory Path : check "Enable project specific settings" then click on "Add JARs..." and select `[your-project]/libs/FragmentArgumentProcessor.jar`.

Start using it :)
