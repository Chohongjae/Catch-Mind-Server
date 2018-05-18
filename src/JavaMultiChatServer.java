import java.io.DataInputStream;

import java.io.DataOutputStream;

import java.io.IOException;

import java.net.ServerSocket;

import java.net.Socket;

import java.util.Arrays;

import java.util.Collections;

import java.util.HashMap;

import java.util.Iterator;

import java.util.List;

import java.util.Random;

import java.util.Set;

 

public class JavaMultiChatServer {

	// HashMap<String, Integer> idMap = new HashMap<String, Integer>();

	/////////////////////////

	HashMap<String, HashMap<String, DataOutputStream>> rooms = new HashMap<String, HashMap<String, DataOutputStream>>();

	HashMap<String, String> drawers = new HashMap<String, String>();

	/////////////////////////

 

	String answer[] = { "호랑이", "베트맨", "의자", "강아지", "시계", "컴퓨터","책상","소방관" };

	Random random = new Random();

	

	int randNum = random.nextInt(answer.length);

	//Room room;

	JavaMultiChatServer() {

		Collections.synchronizedMap(rooms);

		Collections.synchronizedMap(drawers);

	}

 

	public void start() {

		ServerSocket serverSocket = null;

		Socket socket = null;

 

		try {

			serverSocket = new ServerSocket(7777);

			System.out.println("서버가 시작되었습니다");

 

			while (true) {

				socket = serverSocket.accept();

				System.out.println(socket.getInetAddress() + "," + socket.getPort() + "에서 접속하셨습니다");

				// room = new Room(socket);

				// room.start();

				// if(room_key.equals("")){

				//

				// }else{

				// break;

				// }

				ServerReceiver thread = new ServerReceiver(socket);

				thread.start();

 

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

 

	public static void main(String args[]) {

		new JavaMultiChatServer().start();

	}

 

 

 

	public void sendToAll(HashMap<String, DataOutputStream> room, String msg) {

		Iterator it = room.keySet().iterator();

 

		while (it.hasNext()) {

			try {

				String str = String.valueOf(it.next());

				DataOutputStream out = (DataOutputStream) room.get(str);

				System.out.println("str ::::  " + str + ",  " + msg);

				out.writeUTF(msg);

				out.flush();

			} catch (IOException e) {

			}

		}

	}

 

	public void sendToMe(String msg, DataOutputStream out) {

		try {

			out.writeUTF(msg);

			out.flush();

		} catch (IOException e) {

		}

	}

 

	

 

	public void announceDrawer(String roomName, HashMap<String, DataOutputStream> room) {

		Iterator it = room.keySet().iterator();

		String drawer = drawers.get(roomName);

		while (it.hasNext()) {

			try {

				String str = String.valueOf(it.next());

				String msg = "drawer-";

				if (str.equals(drawer))

					msg += "true";

				else

					msg += "false";

				DataOutputStream out = (DataOutputStream) room.get(str);

				// DataOutputStream out1 = (DataOutputStream) clients.get(name);

				// System.out.println("str :::: "+str +", "+msg);

				System.out.println(str + msg);

				out.writeUTF(msg);// +"-"+room_name);

 

				out.flush();

			} catch (IOException e) {

			}

		}

	}

 

	class ServerReceiver extends Thread {

		Socket socket;

		DataInputStream in;

		DataOutputStream out;

		

		ServerReceiver(Socket socket) {

			this.socket = socket;

			try {

				in = new DataInputStream(socket.getInputStream());

				out = new DataOutputStream(socket.getOutputStream());

			} catch (IOException e) {

			}

		}

 

		@Override

		public void run() {

			String name = "";

			String roomName = "";

			try {

				

				name = in.readUTF();

				roomName = in.readUTF();

				// sendToAll(""+room_name);

				// System.out.println(room_name);

				

				// name = getId(name);

				System.out.println(name + "님 접속하셨습니다");

				// clients.put(name, out);

				if (!rooms.containsKey(roomName)) {

					rooms.put(roomName, new HashMap<String, DataOutputStream>());

				}

				rooms.get(roomName).put(name, out);

//				if (drawers.get(roomName) == null)

//					drawers.put(roomName, name);

				sendToAll(rooms.get(roomName), name + "님이 들어오셨습니다.");

 

				//sendToAll(rooms.get(roomName), "");

				sendToAll(rooms.get(roomName), "현재 인원은 -" + rooms.get(roomName).size() + "-명 입니다.");

				if (drawers.get(roomName) == null || rooms.get(roomName).size() == 1) {

					drawers.put(roomName, name);

					sendToAll(rooms.get(roomName) , "hi-" + name);

					// out.writeUTF("hi-" + name);

					System.out.println("여기 로그!! ["+roomName+"]");

					announceDrawer(roomName, rooms.get(roomName)); // 방이름 ,이름

					sendToAll(rooms.get(roomName), "random-" + answer[randNum] + "을 그려주세요.");

				} else {

					DataOutputStream out = (DataOutputStream) rooms.get(roomName).get(name);

					// System.out.println("str :::: "+name +", drawer-false");

					announceDrawer(roomName, rooms.get(roomName));

					out.flush();

				}

				while (in != null) {

					String str = in.readUTF();

 

					System.out.println(str);

 

					if (str.contains("bye")) {

						System.out.println("느으아아앙");

						break;

					}

					// sendToAll(rooms.get(roomName),str);

					///////////////////////////////////////////

					sendToAll(rooms.get(roomName), str);

					///////////////////////////////////////////

					String a[] = str.split("-");

					if (a[0].equals("clear")) {

						if (drawers.get(roomName) != name) {

							sendToMe("clear-" + "지워주세요", out);

						}

					}

					String b[] = a[1].split(",");

					String c = b[2];

					if (c.equals(answer[randNum])) {

						sendToAll(rooms.get(roomName), "answer-" + name + "님이 정답을 맞추셨습니다." + "!");

						System.out.println(name);

						System.out.println("@@@@@@@@@@@");

						sendToAll(rooms.get(roomName), "score-" + name + "-3");

						drawers.put(roomName, null);

						drawers.put(roomName, name);

 

						sendToMe("random-" + answer[randNum] + "을 그려주세요.", out);

						sendToAll(rooms.get(roomName), "hi-" + name);

 

						announceDrawer(roomName, rooms.get(roomName));

					}

 

					System.out.println(str);

 

				}

			} catch (IOException e) {

			} finally {

				// System.out.println("@@@@@@@@@");

				System.out.println(name + "님이 나가셨습니다.");

				sendToAll(rooms.get(roomName), name + "님이 나가셨습니다");

				sendToAll(rooms.get(roomName), "현재 인원은 -" + (rooms.get(roomName).size()-1) + "-명 입니다.");

				rooms.get(roomName).remove(name);

				if (drawers.get(roomName).equals(name)){

					drawers.put(roomName, null);

					

					for(String tempName : rooms.get(roomName).keySet()){

						drawers.put(roomName, tempName);

						//break;

					}

										

 

					System.out.println(drawers.get(roomName)+"**");

					System.out.println(rooms.get(roomName)+"--");

					announceDrawer(roomName, rooms.get(roomName));

					sendToAll(rooms.get(roomName), "hi-" + drawers.get(roomName));

					sendToAll(rooms.get(roomName), "random-" + answer[randNum] + "을 그려주세요.");

					

				}

				

			}

		}

 

	}

 

}

