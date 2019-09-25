package kr.co.itcen.mysite.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import kr.co.itcen.mysite.vo.BoardVo;



public class BoardDao {
	
	//글 쓰기를 위한 insert
	public Boolean insert(BoardVo vo) {
		Boolean result = false;
		
		Connection connection = null;
		PreparedStatement pstmt = null;
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			connection = getConnection();
			
			String sql = "select ifnull(max(g_no)+1,1) from board";
			pstmt = connection.prepareStatement(sql);
			
			rs=pstmt.executeQuery();
			int group_no = 0 ; 
			

			while(rs.next()) {
				int gr_no = rs.getInt(1);
				group_no = gr_no;
			}
						
			String sql1 = "insert into board values(null, ?, ?, 0, now(), ?, ?, 0, ?)";
			pstmt = connection.prepareStatement(sql1);
			pstmt.setString(1,vo.getTitle());
			pstmt.setString(2,vo.getContents());
			pstmt.setInt(3,group_no);
			pstmt.setInt(4, vo.getO_no());
			pstmt.setLong(5, vo.getUser_no());
			int count = pstmt.executeUpdate();
			result = (count == 1);
			

			
		} catch (SQLException e) {
			System.out.println("insert_error:" + e);
		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(stmt != null) {
					stmt.close();
				}
				
				if(pstmt != null) {
					pstmt.close();
				}
				
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;		
	}
	// 답글 쓰기를 위한 newinsert
	public Boolean newinsert(BoardVo vo) {
		Boolean result = false;
		Connection connection = null;
		Statement stmt = null;
		ResultSet rs = null;
		PreparedStatement pstmt = null;

		
		BoardVo parentVo = this.view(vo.getNo());
		
		vo.setG_no(parentVo.getG_no());
		vo.setO_no(parentVo.getO_no() + 1);
		vo.setDepth(parentVo.getDepth() + 1);
		
		try {
			connection = getConnection();

			String sql = "update board " + 
					"set o_no = o_no + 1 " + 
					"where g_no =? and o_no >= ?";
			pstmt = connection.prepareStatement(sql);
			
			pstmt.setLong(1, vo.getG_no());
			pstmt.setLong(2, vo.getO_no());
		
			pstmt.executeUpdate();
			
			
			String sql2 = "insert into board(no,title,contents,hit,reg_date,g_no,o_no,depth,user_no) value(null,?,?,0,now(),?,?,?,?)";
			pstmt = connection.prepareStatement(sql2);
			
			pstmt.setString(1, vo.getTitle());
			pstmt.setString(2, vo.getContents());
			pstmt.setInt(3, vo.getG_no());
			pstmt.setInt(4, vo.getO_no());
			pstmt.setInt(5, vo.getDepth());
			pstmt.setLong(6, vo.getUser_no());
			
			int count = pstmt.executeUpdate();
			result = (count == 1);
		} catch (SQLException e) {
			System.out.println("error:" + e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (pstmt != null) {
					pstmt.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;
	}
	//List를 보여주기 위한 getList
	public List<BoardVo> getList(int page) {
		List<BoardVo> result = new ArrayList<BoardVo>();
		
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			connection = getConnection();
			
			String sql = 
				"select a.user_no, a.title ,b.name, a.hit ,date_format(reg_date, '%Y-%m-%d %h:%i:%s'),depth,a.no,a.g_no,a.o_no"
				+ " from board a, user b "
				+ "where a.user_no =b.no"
				+ " order by a.g_no DESC, a.o_no ASC Limit ?,10" ;
			pstmt = connection.prepareStatement(sql);
			pstmt.setLong(1,page);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				Long user_no = rs.getLong(1);
				String title =rs.getString(2);
				String user_name =rs.getString(3);
				int hit =rs.getInt(4);
				String reg_date=rs.getString(5);
				int depth=rs.getInt(6);
				Long no = rs.getLong(7);
				int g_no = rs.getInt(8);
				int o_no =rs.getInt(9);
				
				BoardVo vo= new BoardVo();
				vo.setUser_no(user_no);
				vo.setTitle(title);
				vo.setUser_name(user_name);
				vo.setHit(hit);
				vo.setReg_date(reg_date);
				vo.setDepth(depth);
				vo.setNo(no);
				vo.setG_no(g_no);
				vo.setO_no(o_no);
				
				result.add(vo);
				
			}
		} catch (SQLException e) {
			System.out.println("select_error:" + e);
		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(pstmt != null) {
					pstmt.close();
				}
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	//게시글 제목을 눌렀을 때 내용을 보여주기 위한 view
	public BoardVo view(long no) {
		BoardVo result = new BoardVo();
		
		Connection connection = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			connection = getConnection();
			
			String sql = " update board set hit=hit+1 where no=?; ";
			pstmt = connection.prepareStatement(sql);
			pstmt.setLong(1,no);
			
			pstmt.executeUpdate();
			
	
			String sql1 = 
				"select no, title, contents, user_no, reg_date, g_no, o_no, depth  from board where no =?" ;
			pstmt = connection.prepareStatement(sql1);
			pstmt.setLong(1,no);
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				Long no1= rs.getLong(1);
				String title =rs.getString(2);
				String contents =rs.getString(3);
				Long user_no = rs.getLong(4);
				String reg_date = rs.getString(5);
				int g_no = rs.getInt(6);
				int o_no = rs.getInt(7);
				int depth = rs.getInt(8);
				
				BoardVo vo= new BoardVo();
				vo.setNo(no1);
				vo.setTitle(title);
				vo.setContents(contents);
				vo.setUser_no(user_no);
				vo.setReg_date(reg_date);
				vo.setG_no(g_no);
				vo.setO_no(o_no);
				vo.setDepth(depth);
				
				result = vo;
			}
		} catch (SQLException e) {
			System.out.println("view_error:" + e);
		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(pstmt != null) {
					pstmt.close();
				}
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	// 게시글을 수정하기 위한 modify
	public boolean modify(BoardVo vo) {
		boolean result = false;

		Connection connection = null;
		PreparedStatement pstmt = null;
		

		try {
			connection = getConnection();

			String sql = "update board set title= ? , contents = ?, reg_date = now() where no = ?";
			pstmt = connection.prepareStatement(sql);
			pstmt.setString(1, vo.getTitle());
			pstmt.setString(2, vo.getContents());
			pstmt.setLong(3,vo.getNo());
			
			int count=pstmt.executeUpdate();
			
			result=(count==1);

		} catch (SQLException e) {
			System.out.println("modify_error:" + e);
		} finally {
			try {
				if(pstmt != null) {
					pstmt.close();
				}

				if(pstmt != null) {
					pstmt.close();
				}

				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return result;		
	}
	//게시글을 삭제 하기위한 delete
	public void delete(long no) {
		
		Connection connection = null;
		PreparedStatement pstmt = null;
		
		try {
			connection = getConnection();
			
			String sql =
				" delete" +
				"   from board" +
				"  where no = ?";
			
			pstmt = connection.prepareStatement(sql);
			pstmt.setLong(1, no);

			
			pstmt.executeUpdate();
			
		} catch (SQLException e) {
			System.out.println("delete_error:" + e);
		} finally {
			try {
				if(pstmt != null) {
					pstmt.close();
				}
				if(connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	
	
	
	
	private Connection getConnection() throws SQLException {
		Connection connection = null;
		
		try {
			Class.forName("org.mariadb.jdbc.Driver");
		
			String url = "jdbc:mariadb://192.168.1.73:3306/webdb?characterEncoding=utf8";
			connection = DriverManager.getConnection(url, "webdb", "webdb");
		
		} catch (ClassNotFoundException e) {
			System.out.println("Fail to Loading Driver:" + e);
		}
		
		return connection;
	}

}
