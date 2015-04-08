from flask import Flask, Response, request
from flask.ext.cors import CORS
from bigquery import get_client

import MySQLdb, os, urllib2, json, base64, Queue

app = Flask(__name__)
app.config['DEBUG'] = True
cors = CORS(app)

# Note: We don't need to call run() since our application is embedded within
# the App Engine WSGI application server.from bigquery import get_client

# Query class for easy requesting data from server
env = os.getenv('SERVER_SOFTWARE')
if (env and env.startswith('Google App Engine/')):
	# Connecting from App Engine
	db = MySQLdb.connect(
		unix_socket='/cloudsql/estirator:estidb',
		user='root')
else:
	# You may also assign an IP Address from the access control
	# page and use it to connect from an external network.
	pass

def execute_query(query_str, parameter=()):
	cursor = db.cursor()
	cursor.execute(query_str, parameter)

	result_set = []
	if cursor.description != None:
		num_fields = len(cursor.description)
		field_names = [i[0] for i in cursor.description]
		for row in cursor.fetchall():
			tmp_obj = {}
			for i in range(0, num_fields):
				tmp_obj[field_names[i]] = row[i]
			result_set.append(tmp_obj)
		# result_set.append(row)

	# print('Last Row ID:' + str(cursor.lastrowid))
	return result_set

def execute_api_call(api_query):
	# Add the username and password.
	# If we knew the realm, we could use it instead of None.
	top_level_url = "https://api.ebay-kleinanzeigen.de/"
	url = top_level_url + "api/" + api_query

	username = 'hpi_hackathon'
	password = 'dsk38a1l'

	request = urllib2.Request(url)
	# You need the replace to handle encodestring adding a trailing newline 
	# (https://docs.python.org/2/library/base64.html#base64.encodestring)
	base64string = base64.encodestring('%s:%s' % (username, password)).replace('\n', '')
	request.add_header("Authorization", "Basic %s" % base64string)   
	result = urllib2.urlopen(request)
	
	# use the opener to fetch a URL
	return json.load(result)
	
def add_to_db(product_id):
	entry_data = execute_api_call('ads/' + str(product_id) + '.json')["{http://www.ebayclassifiedsgroup.com/schema/ad/v1}ad"]["value"]

	img_url = ""
	if len(entry_data) > 0:
		img_url = entry_data["pictures"]["picture"][0]["link"][-1]["href"]
	title = entry_data["title"]["value"]
	price = "0"
	
	if "price" in entry_data and len(entry_data["price"]["amount"]) > 0:
		price = entry_data["price"]["amount"]["value"]

	execute_query('INSERT INTO esti_db.ads (ID, TITLE, IMG_URL, PRICE, AVG_PRICE, AVG_COUNT)' +
					'VALUES (%s, %s,%s,%s,%s, %s)', (product_id, title.replace('"','').encode('latin-1'), img_url, price, 0, 0))

def get_sortedids(product_ids):
	prio_queue = Queue.PriorityQueue()
	# print(product_ids)
	for product_id in product_ids:
		result_set = execute_query('SELECT * FROM esti_db.ads WHERE ID=%s', product_id)
		
		if len(result_set) == 0:
			add_to_db(product_id)
			result_set = execute_query('SELECT * FROM esti_db.ads WHERE ID=%s', product_id)
		# print(result_set)
		# Sort by difference between real price and guess
		# Test for things to gift
		if result_set[0]["AVG_PRICE"] == 0:
			sort_val = 0
		else:
			# Calculate as percentage of differenze between guessed and real val
			sort_val = (result_set[0]["PRICE"] - result_set[0]["AVG_PRICE"]) / result_set[0]["AVG_PRICE"] * 100

		prio_queue.put(( sort_val, result_set[0] ))
	
	if prio_queue.qsize == 0:
		return '[]'	

	# Create sorted array
	sorted_data = []
	while not prio_queue.empty():
		sorted_data.append(prio_queue.get()[1])
	# print('sorted data:', sorted_data)
	return json.dumps(sorted_data, encoding="latin-1")

	
@app.route('/')
def show_all():
	result_set = execute_query('SELECT * FROM esti_db.ads LIMIT 1000')

	"""Return a friendly HTTP greeting."""
	return Response(json.dumps(result_set), mimetype="application/json")

@app.route('/clear/')
def clear_table():
	result_set = execute_query('TRUNCATE table esti_db.ads')
	return 'Cleared all data entries' + str(result_set)

@app.route('/populate/')
def populate_table():
	title = ['Monitore 19" und 22"', 'Hercules 7 Gang Damenfahrrad', 'Bewegungssensor Licht', 'Peugeot 206 135000km,Klima,Servo usw']
	img_url = ['http://i.ebayimg.com/00/s/NTc3WDEwMjQ=/z/qGEAAOSwNSxVE871/$_72.JPG', 'http://i.ebayimg.com/00/s/NTc1WDEwMjQ=/z/Id0AAOSwqu9VJB0Y/$_72.JPG', 'http://i.ebayimg.com/00/s/NzY4WDEwMjQ=/z/QxIAAOSwgjhVJB3Y/$_72.JPG', 'http://i.ebayimg.com/00/s/NzY4WDEwMjQ=/z/2P4AAOSwpDdVJBtj/$_72.JPG']
	prices = [25.0, 100.0, 5.0, 99999.5]
	avg_prices = [10.3, 50.0, 10.0, 10007.2]
	avg_count = [5, 10, 2, 5]

	for i in range(0,4):
		result_set = execute_query('INSERT INTO esti_db.ads (TITLE, IMG_URL, PRICE,AVG_PRICE, AVG_COUNT)' +
									'VALUES (%s,%s,%s,%s, %s)', (title[i], img_url[i], prices[i], avg_prices[i], avg_count[i]))
	return 'Created setup data'

@app.route('/estimate/<product_id>/<new_price>/')
def estimate(product_id, new_price):
	result_set = execute_query('SELECT AVG_PRICE, AVG_COUNT FROM esti_db.ads WHERE ID=%s', product_id)

	if len(result_set) == 0:
		add_to_db(product_id)

	# Calculate new avg
	new_count = result_set[0][1] + 1
	new_val = (result_set[0][1] * result_set[0][0] + float(new_price)) / new_count
	result_set = execute_query('UPDATE esti_db.ads SET AVG_PRICE = %s, AVG_COUNT = %s WHERE ID=%s', (new_val, new_count, product_id))

	return 'Updated data ' + str(result_set)

@app.route('/search/<search_term>/')
def search_request(search_term):
	all_ads_data = execute_api_call('ads.json?q='+ str(search_term + '&pictureRequired=true&size=10'))["{http://www.ebayclassifiedsgroup.com/schema/ad/v1}ads"]["value"]["ad"]
	# print('All ads data', all_ads_data)
	ids = [ad["id"] for ad in all_ads_data]
	# print(ids)
	return get_sortedids(ids)

@app.route('/sorted/', methods = ['POST'])
def sorted_request():
	product_ids = request.get_json(force=True)
	return get_sortedids(product_ids)


@app.route('/get/<product_id>/')
def get(product_id):
	result_set = execute_query('SELECT * FROM esti_db.ads WHERE ID="' + product_id + '"')
	return json.dumps(result_set)

@app.errorhandler(404)
def page_not_found(e):
	"""Return a custom 404 error."""
	return 'Sorry, nothing at this URL.', 404
